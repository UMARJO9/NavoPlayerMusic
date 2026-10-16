package tj.umar.navoplayer.feature.playlists.favorites

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.ObserveFavoriteTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.RestoreFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakeFavoritesRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracks = FakeTrackRepository()
    private val favorites = FakeFavoritesRepository(listOf(TestTracks.longMix.id, 999, TestTracks.alpha.id))
    private val playback = FakePlaybackController()
    private val favoriteTracks = listOf(TestTracks.longMix, TestTracks.alpha)

    private fun viewModel() = FavoritesViewModel(
        observeFavoriteTracks = ObserveFavoriteTracksUseCase(
            favorites,
            ObserveTracksUseCase(tracks, FakeSettingsRepository()),
            mainDispatcherRule.testDispatcher,
        ),
        observePlaybackState = ObservePlaybackStateUseCase(playback),
        playTracks = PlayTracksUseCase(playback),
        shufflePlayTracks = ShufflePlayTracksUseCase(playback),
        togglePlayPause = TogglePlayPauseUseCase(playback),
        removeFavorite = RemoveFavoriteUseCase(favorites),
        restoreFavorite = RestoreFavoriteUseCase(favorites),
    )

    private suspend fun FavoritesViewModel.started(): FavoritesViewModel = apply {
        onIntent(FavoritesIntent.ScreenStarted(hasPermission = true))
        tracks.emit(TestTracks.tracks)
    }

    @Test
    fun `start without permission navigates to welcome`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(FavoritesIntent.ScreenStarted(hasPermission = false))
            assertEquals(FavoritesEffect.NavigateToWelcome, awaitItem())
        }
        assertEquals(0, favorites.observeCalls)
    }

    @Test
    fun `start loads favorites with minutes and missing count`() = runTest {
        val state = viewModel().started().state.value

        assertFalse(state.isLoading)
        assertEquals(favoriteTracks, state.tracks)
        assertEquals(1, state.favorites?.missingTrackCount)
        assertEquals(favoriteTracks.totalDurationMinutes(), state.totalMinutes)
    }

    @Test
    fun `load error can be retried`() = runTest {
        tracks.error = IllegalStateException("scan failed")
        val viewModel = viewModel().started()
        assertTrue(viewModel.state.value.loadFailed)

        tracks.error = null
        viewModel.onIntent(FavoritesIntent.RetryLoad)
        tracks.emit(TestTracks.tracks)

        assertEquals(favoriteTracks, viewModel.state.value.tracks)
    }

    @Test
    fun `play and shuffle do nothing without favorites`() = runTest {
        favorites.removeFavorite(TestTracks.longMix.id)
        favorites.removeFavorite(TestTracks.alpha.id)
        val viewModel = viewModel().started()

        viewModel.onIntent(FavoritesIntent.PlayClicked)
        viewModel.onIntent(FavoritesIntent.ShuffleClicked)

        assertTrue(playback.commands.isEmpty())
    }

    @Test
    fun `play starts favorites from first track`() = runTest {
        viewModel().started().onIntent(FavoritesIntent.PlayClicked)

        assertEquals(listOf(PlaybackCommand.Play(favoriteTracks, 0, PlaybackSource.Favorites)), playback.commands)
    }

    @Test
    fun `play toggles when favorites are active`() = runTest {
        val viewModel = viewModel().started()
        playback.state.emit(TestPlaybackStates.playingAlpha.copy(source = PlaybackSource.Favorites))

        assertTrue(viewModel.state.value.isFavoritesPlaying)
        viewModel.onIntent(FavoritesIntent.PlayClicked)

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), playback.commands)
    }

    @Test
    fun `shuffle plays favorites shuffled`() = runTest {
        viewModel().started().onIntent(FavoritesIntent.ShuffleClicked)

        assertEquals(listOf(PlaybackCommand.PlayShuffled(favoriteTracks, PlaybackSource.Favorites)), playback.commands)
    }

    @Test
    fun `track click plays at index and resumes paused current track`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(FavoritesIntent.TrackClicked(TestTracks.alpha.id))
        playback.state.emit(TestPlaybackStates.pausedAlpha.copy(source = PlaybackSource.Favorites))
        viewModel.onIntent(FavoritesIntent.TrackClicked(TestTracks.alpha.id))

        assertEquals(
            listOf(PlaybackCommand.Play(favoriteTracks, 1, PlaybackSource.Favorites), PlaybackCommand.TogglePlayPause),
            playback.commands,
        )
    }

    @Test
    fun `long press hides track and offers undo`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(FavoritesIntent.TrackLongPressed(TestTracks.alpha.id))
            assertEquals(FavoritesEffect.ShowRemoveUndo(1, TestTracks.alpha.title), awaitItem())
        }
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
        assertEquals(listOf(TestTracks.longMix).totalDurationMinutes(), viewModel.state.value.totalMinutes)
        assertTrue(TestTracks.alpha.id !in favorites.current)
    }

    @Test
    fun `undo restores favorite at original position`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(FavoritesIntent.RemoveTrackRequested(TestTracks.longMix.id))
        favorites.addFavorite(TestTracks.beta.id)
        viewModel.onIntent(FavoritesIntent.UndoRemoveClicked(1))

        assertEquals(listOf(TestTracks.beta.id, TestTracks.longMix.id, 999L, TestTracks.alpha.id), favorites.current)
        assertEquals(listOf(TestTracks.beta, TestTracks.longMix, TestTracks.alpha), viewModel.state.value.tracks)
        assertTrue(viewModel.state.value.removingTrackIds.isEmpty())
    }

    @Test
    fun `undo after dismiss is ignored`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(FavoritesIntent.RemoveTrackRequested(TestTracks.alpha.id))
        viewModel.onIntent(FavoritesIntent.UndoRemoveDismissed(1))
        viewModel.onIntent(FavoritesIntent.UndoRemoveClicked(1))

        assertTrue(favorites.restoreCalls.isEmpty())
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `new removal makes previous undo stale`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(FavoritesIntent.RemoveTrackRequested(TestTracks.alpha.id))
        viewModel.onIntent(FavoritesIntent.RemoveTrackRequested(TestTracks.longMix.id))
        viewModel.onIntent(FavoritesIntent.UndoRemoveClicked(1))
        assertTrue(favorites.restoreCalls.isEmpty())

        viewModel.onIntent(FavoritesIntent.UndoRemoveClicked(2))
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `remove failure brings track back and shows message`() = runTest {
        val viewModel = viewModel().started()
        favorites.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(FavoritesIntent.TrackLongPressed(TestTracks.alpha.id))
            assertEquals(FavoritesEffect.ShowRemoveUndo(1, TestTracks.alpha.title), awaitItem())
            assertEquals(FavoritesEffect.RemoveFailed, awaitItem())
        }
        assertEquals(favoriteTracks, viewModel.state.value.tracks)
    }

    @Test
    fun `restore failure shows message`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(FavoritesIntent.RemoveTrackRequested(TestTracks.alpha.id))
            awaitItem()
            favorites.writeError = IllegalStateException("locked")
            viewModel.onIntent(FavoritesIntent.UndoRemoveClicked(1))
            assertEquals(FavoritesEffect.RestoreFailed, awaitItem())
        }
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `favorite added elsewhere appears`() = runTest {
        val viewModel = viewModel().started()

        favorites.addFavorite(TestTracks.beta.id)

        assertEquals(TestTracks.beta, viewModel.state.value.tracks.first())
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(FavoritesIntent.BackClicked)
            assertEquals(FavoritesEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `stopped screen ignores changes`() = runTest {
        val viewModel = viewModel().started()
        viewModel.onIntent(FavoritesIntent.ScreenStopped)

        favorites.addFavorite(TestTracks.beta.id)

        assertEquals(favoriteTracks, viewModel.state.value.tracks)
    }

    @Test
    fun `repeated removal of hidden track is ignored`() = runTest {
        val viewModel = viewModel().started()
        val gate = CompletableDeferred<Unit>()
        favorites.writeGate = gate

        viewModel.effects.test {
            viewModel.onIntent(FavoritesIntent.TrackLongPressed(TestTracks.alpha.id))
            viewModel.onIntent(FavoritesIntent.TrackLongPressed(TestTracks.alpha.id))
            gate.complete(Unit)
            assertEquals(1, favorites.writeCalls)
            assertEquals(FavoritesEffect.ShowRemoveUndo(1, TestTracks.alpha.title), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `restart after load does not show loading`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(FavoritesIntent.ScreenStopped)
        viewModel.onIntent(FavoritesIntent.ScreenStarted(hasPermission = true))

        assertFalse(viewModel.state.value.isLoading)
    }

    private fun FavoritesViewModel.cleared() {
        ViewModelStore().apply {
            put("favorites", this@cleared)
            clear()
        }
    }

    @Test
    fun `removal write finishes after view model is cleared`() = runTest {
        val viewModel = viewModel().started()
        val gate = CompletableDeferred<Unit>()
        favorites.writeGate = gate

        viewModel.onIntent(FavoritesIntent.RemoveTrackRequested(TestTracks.alpha.id))
        viewModel.cleared()
        gate.complete(Unit)

        assertTrue(TestTracks.alpha.id !in favorites.current)
    }

    @Test
    fun `restore write finishes after view model is cleared`() = runTest {
        val viewModel = viewModel().started()
        viewModel.onIntent(FavoritesIntent.RemoveTrackRequested(TestTracks.alpha.id))
        val gate = CompletableDeferred<Unit>()
        favorites.writeGate = gate

        viewModel.onIntent(FavoritesIntent.UndoRemoveClicked(1))
        viewModel.cleared()
        gate.complete(Unit)

        assertTrue(TestTracks.alpha.id in favorites.current)
    }
}
