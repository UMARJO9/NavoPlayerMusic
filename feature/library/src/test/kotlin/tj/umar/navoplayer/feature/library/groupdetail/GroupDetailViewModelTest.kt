package tj.umar.navoplayer.feature.library.groupdetail

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTrackGroupUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class GroupDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTrackRepository()
    private val playback = FakePlaybackController()

    private val albumKey = TrackGroupKey(TrackGroupType.Album, 10, null)
    private val albumTracks = listOf(TestTracks.alpha, TestTracks.alphaTwo)

    private fun viewModel(key: TrackGroupKey = albumKey) = GroupDetailViewModel(
        key = key,
        observeTrackGroup = ObserveTrackGroupUseCase(ObserveTracksUseCase(repository), mainDispatcherRule.testDispatcher),
        observePlaybackState = ObservePlaybackStateUseCase(playback),
        playTracks = PlayTracksUseCase(playback),
        shufflePlayTracks = ShufflePlayTracksUseCase(playback),
        togglePlayPause = TogglePlayPauseUseCase(playback),
    )

    private suspend fun GroupDetailViewModel.started(): GroupDetailViewModel = apply {
        onIntent(GroupDetailIntent.ScreenStarted(hasPermission = true))
        repository.emit(TestTracks.library)
    }

    @Test
    fun `initial state is loading without subscription`() {
        val viewModel = viewModel()

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(0, repository.observeCalls)
    }

    @Test
    fun `start without permission navigates to welcome`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(GroupDetailIntent.ScreenStarted(hasPermission = false))
            assertEquals(GroupDetailEffect.NavigateToWelcome, awaitItem())
        }
        assertEquals(0, repository.observeCalls)
    }

    @Test
    fun `start loads group and minutes`() = runTest {
        val state = viewModel().started().state.value

        assertFalse(state.isLoading)
        assertEquals(albumTracks, state.tracks)
        assertEquals(5, state.totalMinutes)
    }

    @Test
    fun `missing group is reported`() = runTest {
        val viewModel = viewModel(TrackGroupKey(TrackGroupType.Album, 999, null)).started()

        assertTrue(viewModel.state.value.isMissing)
    }

    @Test
    fun `load error can be retried`() = runTest {
        repository.error = IllegalStateException("scan failed")
        val viewModel = viewModel()
        viewModel.onIntent(GroupDetailIntent.ScreenStarted(hasPermission = true))
        assertTrue(viewModel.state.value.loadFailed)

        repository.error = null
        viewModel.onIntent(GroupDetailIntent.RetryLoad)
        repository.emit(TestTracks.library)

        assertFalse(viewModel.state.value.loadFailed)
        assertEquals(albumTracks, viewModel.state.value.tracks)
    }

    @Test
    fun `track click plays album as queue`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(GroupDetailIntent.TrackClicked(TestTracks.alphaTwo.id))

        assertEquals(listOf(PlaybackCommand.Play(albumTracks, 1, PlaybackSource.Album("First"))), playback.commands)
    }

    @Test
    fun `artist and folder groups use their own sources`() = runTest {
        val artist = viewModel(TrackGroupKey(TrackGroupType.Artist, null, "Ахмад")).started()
        val folder = viewModel(TrackGroupKey(TrackGroupType.Folder, null, "Music/Mixes")).started()

        artist.onIntent(GroupDetailIntent.TrackClicked(TestTracks.namedOnly.id))
        folder.onIntent(GroupDetailIntent.TrackClicked(TestTracks.longMix.id))

        assertEquals(
            listOf(
                PlaybackCommand.Play(listOf(TestTracks.namedOnly), 0, PlaybackSource.Artist("Ахмад")),
                PlaybackCommand.Play(listOf(TestTracks.longMix), 0, PlaybackSource.Folder("Mixes")),
            ),
            playback.commands,
        )
    }

    @Test
    fun `click on paused current track resumes and playing does nothing`() = runTest {
        val viewModel = viewModel().started()

        playback.state.emit(TestPlaybackStates.pausedAlpha.copy(source = PlaybackSource.Album("First")))
        viewModel.onIntent(GroupDetailIntent.TrackClicked(TestTracks.alpha.id))
        playback.state.emit(TestPlaybackStates.playingAlpha.copy(source = PlaybackSource.Album("First")))
        viewModel.onIntent(GroupDetailIntent.TrackClicked(TestTracks.alpha.id))

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), playback.commands)
    }

    @Test
    fun `unknown track click does nothing`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(GroupDetailIntent.TrackClicked(999))

        assertTrue(playback.commands.isEmpty())
    }

    @Test
    fun `shuffle plays group shuffled`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(GroupDetailIntent.ShuffleClicked)

        assertEquals(listOf(PlaybackCommand.PlayShuffled(albumTracks, PlaybackSource.Album("First"))), playback.commands)
    }

    @Test
    fun `back click navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(GroupDetailIntent.BackClicked)
            assertEquals(GroupDetailEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `stop and restart do not duplicate subscriptions`() = runTest {
        val viewModel = viewModel().started()
        viewModel.onIntent(GroupDetailIntent.ScreenStarted(hasPermission = true))
        assertEquals(1, playback.stateSubscribers)

        viewModel.onIntent(GroupDetailIntent.ScreenStopped)
        assertEquals(0, playback.stateSubscribers)

        viewModel.onIntent(GroupDetailIntent.ScreenStarted(hasPermission = true))
        assertEquals(1, playback.stateSubscribers)
        assertEquals(2, repository.observeCalls)
    }

    @Test
    fun `playback state marks current track`() = runTest {
        val viewModel = viewModel().started()

        playback.state.emit(TestPlaybackStates.playingAlpha)

        assertEquals(TestTracks.alpha.id, viewModel.state.value.currentTrackId)
        assertTrue(viewModel.state.value.isPlaying)
    }

    @Test
    fun `current track from another queue switches to group queue`() = runTest {
        val viewModel = viewModel().started()
        playback.state.emit(TestPlaybackStates.playingAlpha)

        viewModel.onIntent(GroupDetailIntent.TrackClicked(TestTracks.alpha.id))

        assertEquals(listOf(PlaybackCommand.Play(albumTracks, 0, PlaybackSource.Album("First"))), playback.commands)
    }

    @Test
    fun `actions on missing group do nothing`() = runTest {
        val viewModel = viewModel(TrackGroupKey(TrackGroupType.Album, 999, null)).started()

        viewModel.onIntent(GroupDetailIntent.ShuffleClicked)
        viewModel.onIntent(GroupDetailIntent.TrackClicked(TestTracks.alpha.id))
        viewModel.onIntent(GroupDetailIntent.SortClicked)

        assertTrue(playback.commands.isEmpty())
    }
}
