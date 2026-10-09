package tj.umar.navoplayer.feature.player.nowplaying

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.usecase.CancelSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.CycleRepeatModeUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveIsFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.SeekToUseCase
import tj.umar.navoplayer.core.domain.usecase.SetFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToNextUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToPreviousUseCase
import tj.umar.navoplayer.core.domain.usecase.StartEndOfTrackSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.StartSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.domain.usecase.ToggleShuffleUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes

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
    private val observeIsFavorite: ObserveIsFavoriteUseCase,
    private val setFavorite: SetFavoriteUseCase,
    private val observeSleepTimer: ObserveSleepTimerUseCase,
    private val startSleepTimer: StartSleepTimerUseCase,
    private val startEndOfTrackSleepTimer: StartEndOfTrackSleepTimerUseCase,
    private val cancelSleepTimer: CancelSleepTimerUseCase,
) : MviViewModel<NowPlayingState, NowPlayingIntent, NowPlayingEffect>(NowPlayingState()) {

    private var stateJob: Job? = null
    private var progressJob: Job? = null
    private var collapseRequested = false
    private var pendingSeekTarget: Long? = null
    private var staleTicksAfterSeek = 0
    private var favoriteJob: Job? = null
    private var favoriteWriteJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var lastFavorite: Pair<Long, Boolean>? = null
    private val currentTrackIds = MutableStateFlow<Long?>(null)
    private var previousRequested = false

    override fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            NowPlayingIntent.ScreenStarted -> startObserving()
            NowPlayingIntent.ScreenStopped -> stopObserving()
            NowPlayingIntent.PlayPauseClicked -> launchCommand { togglePlayPause() }
            NowPlayingIntent.NextClicked -> {
                previousRequested = false
                launchCommand { skipToNext() }
            }
            NowPlayingIntent.PreviousClicked -> {
                previousRequested = true
                launchCommand { skipToPrevious() }
            }
            NowPlayingIntent.ShuffleClicked -> launchCommand { toggleShuffle() }
            NowPlayingIntent.RepeatClicked -> launchCommand { cycleRepeatMode() }
            is NowPlayingIntent.SeekChanged -> setState { copy(seekPreviewMs = intent.positionMs) }
            NowPlayingIntent.SeekFinished -> finishSeek()
            NowPlayingIntent.FavoriteClicked -> toggleFavorite()
            NowPlayingIntent.CollapseClicked -> requestCollapse()
            NowPlayingIntent.QueueClicked -> sendEffect(NowPlayingEffect.OpenQueue)
            NowPlayingIntent.MoreClicked -> Unit
            NowPlayingIntent.SleepTimerClicked -> setState { copy(isSleepTimerSheetVisible = true) }
            NowPlayingIntent.SleepTimerSheetDismissed -> setState { copy(isSleepTimerSheetVisible = false) }
            is NowPlayingIntent.SleepTimerOptionSelected -> onSleepTimerOptionSelected(intent.option)
            NowPlayingIntent.SleepTimerCancelClicked -> onSleepTimerCancelled()
        }
    }

    private fun startObserving() {
        if (stateJob?.isActive != true) {
            stateJob = observePlaybackState().onEach(::onPlaybackState).catch { }.launchIn(viewModelScope)
        }
        if (progressJob?.isActive != true) {
            progressJob = observePlaybackProgress().onEach(::onProgress).catch { }.launchIn(viewModelScope)
        }
        startObservingFavorite()
        if (sleepTimerJob?.isActive != true) {
            sleepTimerJob = observeSleepTimer()
                .onEach { timer -> setState { copy(sleepTimer = timer) } }
                .catch { }
                .launchIn(viewModelScope)
        }
    }

    private fun stopObserving() {
        stateJob?.cancel()
        progressJob?.cancel()
        favoriteJob?.cancel()
        stateJob = null
        progressJob = null
        favoriteJob = null
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        lastFavorite = null
    }

    private fun onPlaybackState(playback: PlaybackState) {
        val track = playback.currentTrack
        if (track == null) {
            requestCollapse()
            return
        }
        val direction = changeDirectionTo(track)
        setState {
            copy(
                isLoading = false,
                track = track,
                nextTrack = playback.nextTrack,
                source = playback.source,
                isPlaying = playback.isPlaying,
                shuffleEnabled = playback.shuffleEnabled,
                repeatMode = playback.repeatMode,
                isFavorite = favoriteFor(track.id),
                trackChangeDirection = direction,
            )
        }
        currentTrackIds.value = track.id
    }

    private fun changeDirectionTo(track: Track): TrackChangeDirection {
        val current = state.value
        val previousTrack = current.track
        if (previousTrack == null || previousTrack.id == track.id) return current.trackChangeDirection
        val backward = previousRequested && track.id != current.nextTrack?.id
        previousRequested = false
        return if (backward) TrackChangeDirection.Backward else TrackChangeDirection.Forward
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
        if (favoriteWriteJob?.isActive == true) return
        val trackId = currentState.track?.id ?: return
        val target = !currentState.isFavorite
        lastFavorite = trackId to target
        setState { copy(isFavorite = target) }
        favoriteWriteJob = viewModelScope.launch {
            setFavorite(trackId, target).onError {
                lastFavorite = trackId to !target
                if (currentState.track?.id == trackId) setState { copy(isFavorite = !target) }
                sendEffect(NowPlayingEffect.ShowMessage(NowPlayingMessage.FavoriteFailed))
            }
        }
    }

    private fun NowPlayingState.favoriteFor(trackId: Long): Boolean {
        val known = lastFavorite
        return when {
            known != null && known.first == trackId -> known.second
            trackId == track?.id -> isFavorite
            else -> false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startObservingFavorite() {
        if (favoriteJob?.isActive == true) return
        favoriteJob = currentTrackIds
            .flatMapLatest { trackId ->
                if (trackId == null) flowOf(null) else observeIsFavorite(trackId).map { trackId to it }
            }
            .onEach { favorite ->
                lastFavorite = favorite
                if (favorite != null && favorite.first == currentState.track?.id) {
                    setState { copy(isFavorite = favorite.second) }
                }
            }
            .catch { lastFavorite = null }
            .launchIn(viewModelScope)
    }

    private fun onSleepTimerOptionSelected(option: SleepTimerOption) {
        val (started, message) = when (option) {
            is SleepTimerOption.Minutes ->
                startSleepTimer(option.minutes.minutes) to NowPlayingMessage.SleepTimerSet(option.minutes)
            SleepTimerOption.EndOfTrack ->
                startEndOfTrackSleepTimer() to NowPlayingMessage.SleepTimerEndOfTrack
        }
        setState { copy(isSleepTimerSheetVisible = false) }
        sendEffect(NowPlayingEffect.ShowMessage(if (started) message else NowPlayingMessage.SleepTimerUnavailable))
    }

    private fun onSleepTimerCancelled() {
        cancelSleepTimer()
        setState { copy(isSleepTimerSheetVisible = false) }
        sendEffect(NowPlayingEffect.ShowMessage(NowPlayingMessage.SleepTimerOff))
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
