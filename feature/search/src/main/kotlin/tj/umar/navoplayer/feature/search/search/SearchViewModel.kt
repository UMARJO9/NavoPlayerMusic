package tj.umar.navoplayer.feature.search.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.SearchResults
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.SearchLibraryUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

private const val QUERY_KEY = "search_query"

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchLibrary: SearchLibraryUseCase,
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val playTracks: PlayTracksUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
) : MviViewModel<SearchState, SearchIntent, SearchEffect>(initialState(savedStateHandle[QUERY_KEY] ?: "")) {

    private val queries = MutableStateFlow(currentState.query)
    private var searchJob: Job? = null
    private var playbackJob: Job? = null

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            SearchIntent.ScreenStopped -> stopObserving()
            is SearchIntent.QueryChanged -> onQueryChanged(intent.query)
            SearchIntent.ClearQueryClicked -> onQueryChanged("")
            SearchIntent.BackClicked -> sendEffect(SearchEffect.NavigateBack)
            is SearchIntent.TrackClicked -> onTrackClicked(intent.trackId)
            is SearchIntent.GroupClicked -> sendEffect(SearchEffect.NavigateToGroup(intent.key))
            SearchIntent.RetryClicked -> retry()
        }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (!hasPermission) {
            sendEffect(SearchEffect.NavigateToWelcome)
            return
        }
        startSearching()
        startObservingPlayback()
    }

    private fun onQueryChanged(query: String) {
        savedStateHandle[QUERY_KEY] = query
        queries.value = query
        setState {
            when {
                query.isBlank() -> copy(query = query, phase = SearchPhase.Idle, resultsQuery = "", tracks = emptyList(), albums = emptyList(), artists = emptyList())
                phase == SearchPhase.Idle -> copy(query = query, phase = SearchPhase.Loading)
                else -> copy(query = query)
            }
        }
    }

    private fun onTrackClicked(trackId: Long) {
        val state = currentState
        val source = PlaybackSource.Search(state.resultsQuery)
        if (trackId == state.currentTrackId && state.currentSource == source) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        val index = state.tracks.indexOfFirst { it.id == trackId }
        if (index < 0) return
        val tracks = state.tracks
        viewModelScope.launch { playTracks(tracks, index, source) }
    }

    private fun retry() {
        searchJob?.cancel()
        searchJob = null
        if (currentState.query.isNotBlank()) setState { copy(phase = SearchPhase.Loading) }
        startSearching()
    }

    @OptIn(FlowPreview::class)
    private fun startSearching() {
        if (searchJob?.isActive == true) return
        searchJob = searchLibrary(queries.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS })
            .onEach(::onResults)
            .catch { setState { copy(phase = SearchPhase.Error) } }
            .launchIn(viewModelScope)
    }

    private fun onResults(results: SearchResults) {
        setState {
            if (results.query.isBlank()) {
                copy(phase = SearchPhase.Idle, resultsQuery = "", tracks = emptyList(), albums = emptyList(), artists = emptyList())
            } else {
                copy(
                    phase = if (results.isEmpty) SearchPhase.NoResults else SearchPhase.Results,
                    resultsQuery = results.query,
                    tracks = results.tracks,
                    albums = results.albums.take(SEARCH_GROUP_LIMIT),
                    artists = results.artists.take(SEARCH_GROUP_LIMIT),
                )
            }
        }
    }

    private fun startObservingPlayback() {
        if (playbackJob?.isActive == true) return
        playbackJob = observePlaybackState()
            .onEach { playback ->
                setState {
                    copy(
                        currentTrackId = playback.currentTrack?.id,
                        currentSource = playback.source,
                        isPlaying = playback.isPlaying,
                    )
                }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        searchJob?.cancel()
        playbackJob?.cancel()
        searchJob = null
        playbackJob = null
    }
}

private fun initialState(query: String): SearchState =
    SearchState(query = query, phase = if (query.isBlank()) SearchPhase.Idle else SearchPhase.Loading)
