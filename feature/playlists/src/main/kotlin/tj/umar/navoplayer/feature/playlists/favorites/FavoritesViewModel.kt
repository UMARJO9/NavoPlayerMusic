package tj.umar.navoplayer.feature.playlists.favorites

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.common.result.onSuccess
import tj.umar.navoplayer.core.domain.model.FavoriteTracks
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.ObserveFavoriteTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.SetFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class FavoritesViewModel @Inject constructor(
    private val observeFavoriteTracks: ObserveFavoriteTracksUseCase,
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val playTracks: PlayTracksUseCase,
    private val shufflePlayTracks: ShufflePlayTracksUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
    private val setFavorite: SetFavoriteUseCase,
) : MviViewModel<FavoritesState, FavoritesIntent, FavoritesEffect>(FavoritesState()) {

    private var favoritesJob: Job? = null
    private var playbackJob: Job? = null
    private var hasLoaded = false

    override fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            FavoritesIntent.ScreenStopped -> stopObserving()
            FavoritesIntent.RetryLoad -> startObservingFavorites()
            FavoritesIntent.BackClicked -> sendEffect(FavoritesEffect.NavigateBack)
            FavoritesIntent.PlayClicked -> onPlayClicked()
            FavoritesIntent.ShuffleClicked -> onShuffleClicked()
            is FavoritesIntent.TrackClicked -> onTrackClicked(intent.trackId)
            is FavoritesIntent.TrackLongPressed -> onTrackLongPressed(intent.trackId)
        }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (!hasPermission) {
            sendEffect(FavoritesEffect.NavigateToWelcome)
            return
        }
        startObservingFavorites()
        startObservingPlayback()
    }

    private fun onPlayClicked() {
        val state = currentState
        if (state.isFavoritesActive) {
            viewModelScope.launch { togglePlayPause() }
            return
        }
        val tracks = state.tracks
        if (tracks.isEmpty()) return
        viewModelScope.launch { playTracks(tracks, 0, PlaybackSource.Favorites) }
    }

    private fun onShuffleClicked() {
        val tracks = currentState.tracks
        if (tracks.isEmpty()) return
        viewModelScope.launch { shufflePlayTracks(tracks, PlaybackSource.Favorites) }
    }

    private fun onTrackClicked(trackId: Long) {
        val state = currentState
        if (trackId == state.currentTrackId && state.isFavoritesActive) {
            if (!state.isPlaying) viewModelScope.launch { togglePlayPause() }
            return
        }
        val tracks = state.tracks
        val index = tracks.indexOfFirst { it.id == trackId }
        if (index < 0) return
        viewModelScope.launch { playTracks(tracks, index, PlaybackSource.Favorites) }
    }

    private fun onTrackLongPressed(trackId: Long) {
        val track = currentState.tracks.firstOrNull { it.id == trackId } ?: return
        viewModelScope.launch {
            setFavorite(track.id, false)
                .onSuccess { changed -> if (changed) sendEffect(FavoritesEffect.TrackRemoved(track.title)) }
                .onError { sendEffect(FavoritesEffect.RemoveFailed) }
        }
    }

    private fun startObservingFavorites() {
        if (favoritesJob?.isActive == true) return
        setState { copy(isLoading = !hasLoaded, loadFailed = false) }
        favoritesJob = observeFavoriteTracks()
            .onEach(::onFavorites)
            .catch { setState { copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun onFavorites(favorites: FavoriteTracks) {
        hasLoaded = true
        setState {
            copy(
                isLoading = false,
                favorites = favorites,
                totalMinutes = favorites.tracks.totalDurationMinutes(),
            )
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
            .catch { setState { copy(currentTrackId = null, currentSource = null, isPlaying = false) } }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        favoritesJob?.cancel()
        playbackJob?.cancel()
        favoritesJob = null
        playbackJob = null
    }
}
