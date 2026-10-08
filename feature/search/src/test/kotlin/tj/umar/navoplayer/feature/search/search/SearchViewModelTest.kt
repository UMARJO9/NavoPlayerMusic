package tj.umar.navoplayer.feature.search.search

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.domain.usecase.ObserveLibraryUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.SearchLibraryUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestSearchTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTrackRepository()
    private val playback = FakePlaybackController()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): SearchViewModel {
        val dispatcher = mainDispatcherRule.testDispatcher
        return SearchViewModel(
            savedStateHandle = savedStateHandle,
            searchLibrary = SearchLibraryUseCase(ObserveLibraryUseCase(ObserveTracksUseCase(repository), dispatcher), dispatcher),
            observePlaybackState = ObservePlaybackStateUseCase(playback),
            playTracks = PlayTracksUseCase(playback),
            togglePlayPause = TogglePlayPauseUseCase(playback),
        )
    }

    private suspend fun SearchViewModel.started(): SearchViewModel = apply {
        onIntent(SearchIntent.ScreenStarted(hasPermission = true))
        repository.emit(TestSearchTracks.library)
    }

    private fun TestScope.typed(viewModel: SearchViewModel, query: String) {
        viewModel.onIntent(SearchIntent.QueryChanged(query))
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)
    }

    @Test
    fun `initial state is idle without subscription`() {
        val viewModel = viewModel()

        assertEquals(SearchPhase.Idle, viewModel.state.value.phase)
        assertEquals(0, repository.observeCalls)
    }

    @Test
    fun `start without permission navigates to welcome`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SearchIntent.ScreenStarted(hasPermission = false))
            assertEquals(SearchEffect.NavigateToWelcome, awaitItem())
        }
    }

    @Test
    fun `typing shows loading then results after debounce`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(SearchIntent.QueryChanged("black"))
        assertEquals("black", viewModel.state.value.query)
        assertEquals(SearchPhase.Loading, viewModel.state.value.phase)

        advanceTimeBy(SEARCH_DEBOUNCE_MS - 50)
        assertTrue(viewModel.state.value.tracks.isEmpty())

        advanceTimeBy(100)
        assertEquals(SearchPhase.Results, viewModel.state.value.phase)
        assertEquals(listOf("Blackbird", "Back in Black"), viewModel.state.value.tracks.map { it.title })
    }

    @Test
    fun `rapid typing searches only the last query`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(SearchIntent.QueryChanged("b"))
        viewModel.onIntent(SearchIntent.QueryChanged("bl"))
        typed(viewModel, "ёлка")

        assertEquals("ёлка", viewModel.state.value.resultsQuery)
        assertEquals(listOf("Ёлка"), viewModel.state.value.tracks.map { it.title })
    }

    @Test
    fun `queries reuse one library subscription`() = runTest {
        val viewModel = viewModel().started()

        typed(viewModel, "black")
        typed(viewModel, "ватан")

        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `clearing query returns to idle`() = runTest {
        val viewModel = viewModel().started()
        typed(viewModel, "black")

        viewModel.onIntent(SearchIntent.ClearQueryClicked)

        val state = viewModel.state.value
        assertEquals("", state.query)
        assertEquals(SearchPhase.Idle, state.phase)
        assertTrue(state.tracks.isEmpty())
    }

    @Test
    fun `no matches shows no results`() = runTest {
        val viewModel = viewModel().started()

        typed(viewModel, "zzzz")

        assertEquals(SearchPhase.NoResults, viewModel.state.value.phase)
        assertEquals("zzzz", viewModel.state.value.resultsQuery)
    }

    @Test
    fun `groups are limited`() = runTest {
        val manyArtists = (1L..6L).map { TestSearchTracks.blackbird.copy(id = 100 + it, artist = "Black $it", artistId = it) }
        val viewModel = viewModel()
        viewModel.onIntent(SearchIntent.ScreenStarted(hasPermission = true))
        repository.emit(manyArtists)

        typed(viewModel, "black")

        assertEquals(SEARCH_GROUP_LIMIT, viewModel.state.value.artists.size)
    }

    @Test
    fun `library change refreshes results`() = runTest {
        val viewModel = viewModel().started()
        typed(viewModel, "black")

        repository.emit(listOf(TestSearchTracks.blackbird))

        assertEquals(listOf("Blackbird"), viewModel.state.value.tracks.map { it.title })
    }

    @Test
    fun `error can be retried`() = runTest {
        repository.error = IllegalStateException("scan failed")
        val viewModel = viewModel()
        viewModel.onIntent(SearchIntent.ScreenStarted(hasPermission = true))
        typed(viewModel, "black")
        assertEquals(SearchPhase.Error, viewModel.state.value.phase)

        repository.error = null
        viewModel.onIntent(SearchIntent.RetryClicked)
        repository.emit(TestSearchTracks.library)
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)

        assertEquals(SearchPhase.Results, viewModel.state.value.phase)
    }

    @Test
    fun `back and group clicks navigate`() = runTest {
        val viewModel = viewModel()
        val key = TrackGroupKey(TrackGroupType.Artist, 10, null)

        viewModel.effects.test {
            viewModel.onIntent(SearchIntent.BackClicked)
            assertEquals(SearchEffect.NavigateBack, awaitItem())
            viewModel.onIntent(SearchIntent.GroupClicked(key))
            assertEquals(SearchEffect.NavigateToGroup(key), awaitItem())
        }
    }

    @Test
    fun `track click plays matched tracks with search source`() = runTest {
        val viewModel = viewModel().started()
        typed(viewModel, "black")

        viewModel.onIntent(SearchIntent.TrackClicked(TestSearchTracks.backInBlack.id))

        val tracks = listOf(TestSearchTracks.blackbird, TestSearchTracks.backInBlack)
        assertEquals(listOf(PlaybackCommand.Play(tracks, 1, PlaybackSource.Search("black"))), playback.commands)
    }

    @Test
    fun `current track from same search toggles when paused and ignores when playing`() = runTest {
        val viewModel = viewModel().started()
        typed(viewModel, "black")
        val searchSource = PlaybackSource.Search("black")
        val current = TestPlaybackStates.pausedAlpha.copy(currentTrack = TestSearchTracks.blackbird, source = searchSource)

        playback.state.emit(current)
        viewModel.onIntent(SearchIntent.TrackClicked(TestSearchTracks.blackbird.id))
        playback.state.emit(current.copy(isPlaying = true))
        viewModel.onIntent(SearchIntent.TrackClicked(TestSearchTracks.blackbird.id))

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), playback.commands)
    }

    @Test
    fun `current track from another source switches queue`() = runTest {
        val viewModel = viewModel().started()
        typed(viewModel, "black")
        playback.state.emit(TestPlaybackStates.playingAlpha.copy(currentTrack = TestSearchTracks.blackbird))

        viewModel.onIntent(SearchIntent.TrackClicked(TestSearchTracks.blackbird.id))

        assertTrue(playback.commands.single() is PlaybackCommand.Play)
    }

    @Test
    fun `stop and restart resubscribe and keep query`() = runTest {
        val viewModel = viewModel().started()
        typed(viewModel, "black")

        viewModel.onIntent(SearchIntent.ScreenStopped)
        viewModel.onIntent(SearchIntent.ScreenStarted(hasPermission = true))

        assertEquals("black", viewModel.state.value.query)
        assertEquals(2, repository.observeCalls)
    }

    @Test
    fun `query is restored from saved state`() {
        val viewModel = viewModel(SavedStateHandle(mapOf("search_query" to "ватан")))

        assertEquals("ватан", viewModel.state.value.query)
        assertEquals(SearchPhase.Loading, viewModel.state.value.phase)
    }
}
