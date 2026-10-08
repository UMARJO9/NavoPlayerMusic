package tj.umar.navoplayer.feature.library.library

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.domain.usecase.CreatePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistsOverviewUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveLibraryUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakeFavoritesRepository
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTrackRepository()
    private val playback = FakePlaybackController()
    private val playlists = FakePlaylistRepository(TestPlaylists.all)
    private val playlistTracks = FakeTrackRepository()
    private val favorites = FakeFavoritesRepository(listOf(TestTracks.alpha.id))
    private val viewModel = LibraryViewModel(
        observeLibrary = ObserveLibraryUseCase(ObserveTracksUseCase(repository, FakeSettingsRepository()), mainDispatcherRule.testDispatcher),
        observePlaybackState = ObservePlaybackStateUseCase(playback),
        playTracks = PlayTracksUseCase(playback),
        shufflePlayTracks = ShufflePlayTracksUseCase(playback),
        togglePlayPause = TogglePlayPauseUseCase(playback),
        observePlaylistsOverview = ObservePlaylistsOverviewUseCase(
            playlists,
            favorites,
            ObserveTracksUseCase(playlistTracks, FakeSettingsRepository()),
            mainDispatcherRule.testDispatcher,
        ),
        createPlaylist = CreatePlaylistUseCase(playlists),
    )

    @Test
    fun `initial state is loading tracks tab without subscription`() {
        val state = viewModel.state.value

        assertEquals(LibraryTab.Tracks, state.selectedTab)
        assertTrue(state.isLoadingTracks)
        assertEquals(0, repository.observeCalls)
    }

    @Test
    fun `TabSelected updates selected tab`() = runTest {
        viewModel.state.test {
            assertEquals(LibraryTab.Tracks, awaitItem().selectedTab)
            viewModel.onIntent(LibraryIntent.TabSelected(LibraryTab.Albums))
            assertEquals(LibraryTab.Albums, awaitItem().selectedTab)
        }
    }

    @Test
    fun `TabSelected with current tab emits nothing`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(LibraryIntent.TabSelected(LibraryTab.Tracks))
            expectNoEvents()
        }
    }

    @Test
    fun `start without permission navigates to welcome`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = false))
            assertEquals(LibraryEffect.NavigateToWelcome, awaitItem())
        }
        assertEquals(0, repository.observeCalls)
    }

    @Test
    fun `start with permission loads tracks and total minutes`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(TestTracks.tracks)

        val state = viewModel.state.value
        assertFalse(state.isLoadingTracks)
        assertEquals(TestTracks.tracks, state.tracks)
        assertEquals(66, state.totalMinutes)
    }

    @Test
    fun `total minutes round to nearest minute`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))

        repository.emit(listOf(TestTracks.alpha.copy(durationMs = 29_999)))
        assertEquals(0, viewModel.state.value.totalMinutes)

        repository.emit(listOf(TestTracks.alpha.copy(durationMs = 30_000)))
        assertEquals(1, viewModel.state.value.totalMinutes)
    }

    @Test
    fun `empty library stops loading with no tracks`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(emptyList())

        val state = viewModel.state.value
        assertFalse(state.isLoadingTracks)
        assertTrue(state.tracks.isEmpty())
        assertEquals(0, state.totalMinutes)
    }

    @Test
    fun `repository error marks load as failed`() {
        repository.error = IllegalStateException("scan failed")

        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))

        val state = viewModel.state.value
        assertFalse(state.isLoadingTracks)
        assertTrue(state.tracksLoadFailed)
    }

    @Test
    fun `later emission updates tracks`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(listOf(TestTracks.alpha))
        repository.emit(TestTracks.tracks)

        assertEquals(TestTracks.tracks, viewModel.state.value.tracks)
    }

    @Test
    fun `repeated starts observe tracks once`() {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))

        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `retry after failure observes tracks again`() = runTest {
        repository.error = IllegalStateException("scan failed")
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.error = null

        viewModel.onIntent(LibraryIntent.RetryLoadTracks)
        repository.emit(TestTracks.tracks)

        val state = viewModel.state.value
        assertEquals(2, repository.observeCalls)
        assertFalse(state.tracksLoadFailed)
        assertEquals(TestTracks.tracks, state.tracks)
    }

    @Test
    fun `retry while observing does not subscribe twice`() {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        viewModel.onIntent(LibraryIntent.RetryLoadTracks)

        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `screen stop ends observation and start resumes it`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(TestTracks.tracks)

        viewModel.onIntent(LibraryIntent.ScreenStopped)
        repository.emit(listOf(TestTracks.alpha))
        assertEquals(TestTracks.tracks, viewModel.state.value.tracks)

        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        assertEquals(2, repository.observeCalls)
        assertEquals(listOf(TestTracks.alpha), viewModel.state.value.tracks)
    }

    @Test
    fun `header and sort clicks change nothing yet`() = runTest {
        val before = viewModel.state.value

        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.SettingsClicked)
            viewModel.onIntent(LibraryIntent.SortClicked)

            expectNoEvents()
        }
        assertEquals(before, viewModel.state.value)
    }

    @Test
    fun `restart after loading does not show spinner again`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(emptyList())
        viewModel.onIntent(LibraryIntent.ScreenStopped)

        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))

        assertFalse(viewModel.state.value.isLoadingTracks)
    }

    private suspend fun startWithTracks() {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(TestTracks.tracks)
    }

    @Test
    fun `track click plays all tracks from that track`() = runTest {
        startWithTracks()

        viewModel.onIntent(LibraryIntent.TrackClicked(TestTracks.beta.id))

        assertEquals(
            listOf(PlaybackCommand.Play(TestTracks.tracks, 1, PlaybackSource.AllTracks)),
            playback.commands,
        )
    }

    @Test
    fun `unknown track click is ignored`() = runTest {
        startWithTracks()

        viewModel.onIntent(LibraryIntent.TrackClicked(999))

        assertTrue(playback.commands.isEmpty())
    }

    @Test
    fun `click on paused current track resumes`() = runTest {
        startWithTracks()
        playback.state.emit(TestPlaybackStates.pausedAlpha)

        viewModel.onIntent(LibraryIntent.TrackClicked(TestTracks.alpha.id))

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), playback.commands)
    }

    @Test
    fun `click on playing current track does nothing`() = runTest {
        startWithTracks()
        playback.state.emit(TestPlaybackStates.playingAlpha)

        viewModel.onIntent(LibraryIntent.TrackClicked(TestTracks.alpha.id))

        assertTrue(playback.commands.isEmpty())
    }

    @Test
    fun `shuffle click plays shuffled library`() = runTest {
        startWithTracks()

        viewModel.onIntent(LibraryIntent.ShuffleClicked)

        assertEquals(listOf(PlaybackCommand.PlayShuffled(TestTracks.tracks, PlaybackSource.AllTracks)), playback.commands)
    }

    @Test
    fun `shuffle click without tracks does nothing`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(emptyList())

        viewModel.onIntent(LibraryIntent.ShuffleClicked)

        assertTrue(playback.commands.isEmpty())
    }

    @Test
    fun `playback state marks current track`() = runTest {
        startWithTracks()
        playback.state.emit(TestPlaybackStates.playingAlpha)

        val state = viewModel.state.value
        assertEquals(TestTracks.alpha.id, state.currentTrackId)
        assertTrue(state.isPlaying)
        assertTrue(state.hasActivePlayback)
    }

    @Test
    fun `screen stop ends playback subscription`() = runTest {
        startWithTracks()
        assertEquals(1, playback.stateSubscribers)

        viewModel.onIntent(LibraryIntent.ScreenStopped)

        assertEquals(0, playback.stateSubscribers)
    }

    @Test
    fun `library emission fills albums artists and folders`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        repository.emit(TestTracks.library)

        val state = viewModel.state.value
        assertEquals(4, state.albums.size)
        assertEquals(5, state.artists.size)
        assertEquals(4, state.folders.size)
    }

    @Test
    fun `group click navigates to group`() = runTest {
        val key = TrackGroupKey(TrackGroupType.Album, 10, null)

        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.GroupClicked(key))
            assertEquals(LibraryEffect.NavigateToGroup(key), awaitItem())
        }
    }

    @Test
    fun `current track from album queue switches to all tracks`() = runTest {
        startWithTracks()
        playback.state.emit(TestPlaybackStates.playingAlpha.copy(source = PlaybackSource.Album("First")))

        viewModel.onIntent(LibraryIntent.TrackClicked(TestTracks.alpha.id))

        assertEquals(
            listOf(PlaybackCommand.Play(TestTracks.tracks, 0, PlaybackSource.AllTracks)),
            playback.commands,
        )
    }

    @Test
    fun `search click navigates to search`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.SearchClicked)
            assertEquals(LibraryEffect.NavigateToSearch, awaitItem())
        }
    }

    @Test
    fun `start loads playlist summaries`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        playlistTracks.emit(TestTracks.tracks)

        val state = viewModel.state.value
        assertFalse(state.isLoadingPlaylists)
        assertEquals(listOf(TestPlaylists.empty.id, TestPlaylists.morning.id), state.playlists.map { it.id })
    }

    @Test
    fun `playlist load failure can be retried`() = runTest {
        playlists.observeError = IllegalStateException("db closed")
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        playlistTracks.emit(TestTracks.tracks)
        assertTrue(viewModel.state.value.playlistsLoadFailed)

        playlists.observeError = null
        viewModel.onIntent(LibraryIntent.RetryLoadPlaylists)

        assertFalse(viewModel.state.value.playlistsLoadFailed)
        assertEquals(2, viewModel.state.value.playlists.size)
    }

    @Test
    fun `playlist click navigates to playlist`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.PlaylistClicked(TestPlaylists.morning.id))
            assertEquals(LibraryEffect.NavigateToPlaylist(TestPlaylists.morning.id), awaitItem())
        }
    }

    @Test
    fun `create playlist dialog opens and dismisses`() {
        viewModel.onIntent(LibraryIntent.CreatePlaylistClicked)
        assertTrue(viewModel.state.value.isCreatePlaylistSheetVisible)

        viewModel.onIntent(LibraryIntent.CreatePlaylistDismissed)
        assertFalse(viewModel.state.value.isCreatePlaylistSheetVisible)
    }

    @Test
    fun `confirmed playlist is created and opened`() = runTest {
        viewModel.onIntent(LibraryIntent.CreatePlaylistClicked)

        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.CreatePlaylistConfirmed("Дорога"))
            val effect = awaitItem() as LibraryEffect.NavigateToPlaylist
            assertEquals("Дорога", playlists.current.single { it.id == effect.playlistId }.name)
        }
        assertFalse(viewModel.state.value.isCreatePlaylistSheetVisible)
    }

    @Test
    fun `failed playlist creation shows error`() = runTest {
        playlists.writeError = IllegalStateException("disk full")

        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.CreatePlaylistConfirmed("Дорога"))
            assertEquals(LibraryEffect.ShowCreatePlaylistFailed, awaitItem())
        }
    }

    @Test
    fun `track long press opens add to playlist`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.TrackLongPressed(TestTracks.alpha.id))
            assertEquals(LibraryEffect.OpenAddToPlaylist(listOf(TestTracks.alpha.id)), awaitItem())
        }
    }

    @Test
    fun `double confirm creates one playlist`() = runTest {
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate

        viewModel.onIntent(LibraryIntent.CreatePlaylistConfirmed("Дорога"))
        viewModel.onIntent(LibraryIntent.CreatePlaylistConfirmed("Дорога"))
        gate.complete(Unit)

        assertEquals(1, playlists.writeCalls)
        assertEquals(1, playlists.current.count { it.name == "Дорога" })
    }

    @Test
    fun `start loads favorites summary with playlists`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        playlistTracks.emit(TestTracks.tracks)

        val favoritesSummary = viewModel.state.value.favorites
        assertEquals(1, favoritesSummary.trackCount)
        assertEquals(TestTracks.alpha.durationMs, favoritesSummary.durationMs)
    }

    @Test
    fun `favorites click navigates to favorites`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.FavoritesClicked)
            assertEquals(LibraryEffect.NavigateToFavorites, awaitItem())
        }
    }

    @Test
    fun `restart does not show playlists loading again`() = runTest {
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))
        playlistTracks.emit(TestTracks.tracks)

        viewModel.onIntent(LibraryIntent.ScreenStopped)
        viewModel.onIntent(LibraryIntent.ScreenStarted(hasPermission = true))

        assertFalse(viewModel.state.value.isLoadingPlaylists)
    }
}
