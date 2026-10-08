package tj.umar.navoplayer.feature.player.nowplaying

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.usecase.CycleRepeatModeUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.SeekToUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToNextUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToPreviousUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.domain.usecase.ToggleShuffleUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject
import kotlin.math.abs

private const val SEEK_TOLERANCE_MILLIS = 1_500L
private const val MAX_STALE_TICKS = 4

@HiltViewModel
internal class NowPlayingViewModel @Inject constructor(
    private val observePlaybackState: ObservePlaybackStateUseCase,
    private val observePlaybackProgress: ObservePlaybackProgressUseCase,
    private val togglePlayPause: TogglePlayPauseUseCase,
    private val skipToNext: SkipToNextUseCase,
    private val skipToPrevious: SkipToPreviousUseCase,
    private val seekTo: SeekToUseCase,
    private val toggleShuffle: ToggleShuffleUseCase,
    private val cycleRepeatMode: CycleRepeatModeUseCase,
) : MviViewModel<NowPlayingState, NowPlayingIntent, NowPlayingEffect>(NowPlayingState()) {

    private var stateJob: Job? = null
    private var progressJob: Job? = null
    private var collapseRequested = false
    private var pendingSeekTarget: Long? = null
    private var staleTicksAfterSeek = 0
    private val favoriteTrackIds = mutableSetOf<Long>()

    override fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            NowPlayingIntent.ScreenStarted -> startObserving()
            NowPlayingIntent.ScreenStopped -> stopObserving()
            NowPlayingIntent.PlayPauseClicked -> launchCommand { togglePlayPause() }
            NowPlayingIntent.NextClicked -> launchCommand { skipToNext() }
            NowPlayingIntent.PreviousClicked -> launchCommand { skipToPrevious() }
            NowPlayingIntent.ShuffleClicked -> launchCommand { toggleShuffle() }
            NowPlayingIntent.RepeatClicked -> launchCommand { cycleRepeatMode() }
            is NowPlayingIntent.SeekChanged -> setState { copy(seekPreviewMs = intent.positionMs) }
            NowPlayingIntent.SeekFinished -> finishSeek()
            NowPlayingIntent.FavoriteClicked -> toggleFavorite()
            NowPlayingIntent.CollapseClicked -> requestCollapse()
            NowPlayingIntent.MoreClicked,
            NowPlayingIntent.QueueClicked,
            NowPlayingIntent.SleepTimerClicked -> Unit
        }
    }

    private fun startObserving() {
        if (stateJob?.isActive != true) {
            stateJob = observePlaybackState().onEach(::onPlaybackState).catch { }.launchIn(viewModelScope)
        }
        if (progressJob?.isActive != true) {
            progressJob = observePlaybackProgress().onEach(::onProgress).catch { }.launchIn(viewModelScope)
        }
    }

    private fun stopObserving() {
        stateJob?.cancel()
        progressJob?.cancel()
        stateJob = null
        progressJob = null
    }

    private fun onPlaybackState(playback: PlaybackState) {
        val track = playback.currentTrack
        if (track == null) {
            requestCollapse()
            return
        }
        setState {
            copy(
                isLoading = false,
                track = track,
                nextTrack = playback.nextTrack,
                source = playback.source,
                isPlaying = playback.isPlaying,
                shuffleEnabled = playback.shuffleEnabled,
                repeatMode = playback.repeatMode,
                isFavorite = track.id in favoriteTrackIds,
            )
        }
    }

    private fun onProgress(progress: PlaybackProgress) {
        val target = pendingSeekTarget
        if (target != null) {
            staleTicksAfterSeek++
            val stale = abs(progress.positionMs - target) > SEEK_TOLERANCE_MILLIS
            if (stale && staleTicksAfterSeek <= MAX_STALE_TICKS) {
                setState { copy(durationMs = progress.durationMs) }
                return
            }
            pendingSeekTarget = null
        }
        setState { copy(positionMs = progress.positionMs, durationMs = progress.durationMs) }
    }

    private fun finishSeek() {
        val target = currentState.seekPreviewMs ?: return
        pendingSeekTarget = target
        staleTicksAfterSeek = 0
        setState { copy(positionMs = target, seekPreviewMs = null) }
        launchCommand { seekTo(target) }
    }

    private fun toggleFavorite() {
        val trackId = currentState.track?.id ?: return
        val favorite = if (trackId in favoriteTrackIds) {
            favoriteTrackIds.remove(trackId)
            false
        } else {
            favoriteTrackIds.add(trackId)
            true
        }
        setState { copy(isFavorite = favorite) }
    }

    private fun requestCollapse() {
        if (collapseRequested) return
        collapseRequested = true
        sendEffect(NowPlayingEffect.Collapse)
    }

    private fun launchCommand(command: suspend () -> Unit) {
        viewModelScope.launch { command() }
    }
}
