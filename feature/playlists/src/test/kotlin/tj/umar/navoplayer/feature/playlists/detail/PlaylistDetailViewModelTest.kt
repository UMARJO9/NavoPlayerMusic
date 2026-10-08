package tj.umar.navoplayer.feature.playlists.detail

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.DeletePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveTrackFromPlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.RenamePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class PlaylistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracks = FakeTrackRepository()
    private val playlists = FakePlaylistRepository(TestPlaylists.all)
    private val playback = FakePlaybackController()

    private val morning = TestPlaylists.morning
    private val morningTracks = listOf(TestTracks.longMix, TestTracks.alpha)
    private val morningSource = PlaybackSource.Playlist(morning.id, morning.name)

    private fun viewModel(playlistId: Long = morning.id) = PlaylistDetailViewModel(
        playlistId = playlistId,
        observePlaylist = ObservePlaylistUseCase(playlists, ObserveTracksUseCase(tracks), mainDispatcherRule.testDispatcher),
        observePlaybackState = ObservePlaybackStateUseCase(playback),
        playTracks = PlayTracksUseCase(playback),
        shufflePlayTracks = ShufflePlayTracksUseCase(playback),
        togglePlayPause = TogglePlayPauseUseCase(playback),
        renamePlaylist = RenamePlaylistUseCase(playlists),
        deletePlaylist = DeletePlaylistUseCase(playlists),
        removeTrackFromPlaylist = RemoveTrackFromPlaylistUseCase(playlists),
    )

    private suspend fun PlaylistDetailViewModel.started(): PlaylistDetailViewModel = apply {
        onIntent(PlaylistDetailIntent.ScreenStarted(hasPermission = true))
        tracks.emit(TestTracks.tracks)
    }

    @Test
    fun `initial state is loading without subscription`() {
        val viewModel = viewModel()

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(0, playlists.observeCalls)
    }

    @Test
    fun `start without permission navigates to welcome`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.ScreenStarted(hasPermission = false))
            assertEquals(PlaylistDetailEffect.NavigateToWelcome, awaitItem())
        }
        assertEquals(0, playlists.observeCalls)
    }

    @Test
    fun `start loads available tracks and minutes`() = runTest {
        val state = viewModel().started().state.value

        assertFalse(state.isLoading)
        assertEquals(morningTracks, state.tracks)
        assertEquals(1, state.playlist?.missingTrackCount)
        assertEquals(morningTracks.totalDurationMinutes(), state.totalMinutes)
    }

    @Test
    fun `unknown playlist is reported missing`() = runTest {
        val state = viewModel(playlistId = 404).started().state.value

        assertTrue(state.isMissing)
    }

    @Test
    fun `load error can be retried`() = runTest {
        tracks.error = IllegalStateException("scan failed")
        val viewModel = viewModel().started()
        assertTrue(viewModel.state.value.loadFailed)

        tracks.error = null
        viewModel.onIntent(PlaylistDetailIntent.RetryLoad)
        tracks.emit(TestTracks.tracks)

        assertFalse(viewModel.state.value.loadFailed)
        assertEquals(morningTracks, viewModel.state.value.tracks)
    }

    @Test
    fun `play starts playlist from first track`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.PlayClicked)

        assertEquals(listOf(PlaybackCommand.Play(morningTracks, 0, morningSource)), playback.commands)
    }

    @Test
    fun `play toggles when playlist is already active`() = runTest {
        val viewModel = viewModel().started()
        playback.state.emit(TestPlaybackStates.playingAlpha.copy(source = PlaybackSource.Playlist(morning.id, "Old name")))

        assertTrue(viewModel.state.value.isPlaylistPlaying)
        viewModel.onIntent(PlaylistDetailIntent.PlayClicked)

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), playback.commands)
    }

    @Test
    fun `shuffle plays shuffled and ignores empty playlist`() = runTest {
        viewModel().started().onIntent(PlaylistDetailIntent.ShuffleClicked)
        viewModel(TestPlaylists.empty.id).started().onIntent(PlaylistDetailIntent.ShuffleClicked)

        assertEquals(listOf(PlaybackCommand.PlayShuffled(morningTracks, morningSource)), playback.commands)
    }

    @Test
    fun `track click plays at index and resumes current paused track`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.TrackClicked(TestTracks.alpha.id))
        playback.state.emit(TestPlaybackStates.pausedAlpha.copy(source = morningSource))
        viewModel.onIntent(PlaylistDetailIntent.TrackClicked(TestTracks.alpha.id))

        assertEquals(
            listOf(PlaybackCommand.Play(morningTracks, 1, morningSource), PlaybackCommand.TogglePlayPause),
            playback.commands,
        )
    }

    @Test
    fun `long press asks to remove and confirm removes track`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.TrackLongPressed(TestTracks.alpha.id))
        assertEquals(
            PlaylistDetailDialog.ConfirmRemoveTrack(TestTracks.alpha.id, TestTracks.alpha.title),
            viewModel.state.value.dialog,
        )
        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackConfirmed)

        assertNull(viewModel.state.value.dialog)
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `remove failure shows message`() = runTest {
        val viewModel = viewModel().started()
        playlists.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.TrackLongPressed(TestTracks.alpha.id))
            viewModel.onIntent(PlaylistDetailIntent.RemoveTrackConfirmed)
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RemoveFailed), awaitItem())
        }
    }

    @Test
    fun `rename updates name`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.RenameClicked)
        assertEquals(PlaylistDetailDialog.Rename(morning.name), viewModel.state.value.dialog)
        viewModel.onIntent(PlaylistDetailIntent.RenameConfirmed("  Вечер "))

        assertNull(viewModel.state.value.dialog)
        assertEquals("Вечер", viewModel.state.value.playlist?.name)
    }

    @Test
    fun `rename failure shows message`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.RenameConfirmed("   "))
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RenameFailed), awaitItem())
        }
    }

    @Test
    fun `delete navigates back without missing flash`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.DeleteClicked)
            assertEquals(PlaylistDetailDialog.ConfirmDelete, viewModel.state.value.dialog)
            viewModel.onIntent(PlaylistDetailIntent.DeleteConfirmed)
            assertEquals(PlaylistDetailEffect.NavigateBack, awaitItem())
        }
        assertFalse(viewModel.state.value.isMissing)
        assertTrue(playlists.current.none { it.id == morning.id })
    }

    @Test
    fun `delete failure keeps playlist and shows message`() = runTest {
        val viewModel = viewModel().started()
        playlists.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.DeleteConfirmed)
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.DeleteFailed), awaitItem())
        }
        assertFalse(viewModel.state.value.isDeleting)
    }

    @Test
    fun `dialog dismiss closes dialog`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.DeleteClicked)
        viewModel.onIntent(PlaylistDetailIntent.DialogDismissed)

        assertNull(viewModel.state.value.dialog)
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.BackClicked)
            assertEquals(PlaylistDetailEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `stop and restart resubscribe`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.ScreenStopped)
        viewModel.onIntent(PlaylistDetailIntent.ScreenStarted(hasPermission = true))

        assertEquals(2, playlists.observeCalls)
        assertEquals(morningTracks, viewModel.state.value.tracks)
    }
}
