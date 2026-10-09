package tj.umar.navoplayer.feature.player.trackactions

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.EnqueueOutcome
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.domain.usecase.EnqueueTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.GetTracksUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class TrackActionsViewModel @Inject constructor(
    private val getTracks: GetTracksUseCase,
    private val enqueueTracks: EnqueueTracksUseCase,
) : MviViewModel<TrackActionsState, TrackActionsIntent, TrackActionsEffect>(TrackActionsState()) {

    private var loadJob: Job? = null

    override fun onIntent(intent: TrackActionsIntent) {
        when (intent) {
            is TrackActionsIntent.Opened -> onOpened(intent.request)
            TrackActionsIntent.PlayNextClicked -> enqueue(QueueInsertion.Next)
            TrackActionsIntent.AddToQueueClicked -> enqueue(QueueInsertion.Last)
            TrackActionsIntent.AddToPlaylistClicked -> onAddToPlaylistClicked()
        }
    }

    private fun onOpened(request: TrackActionsRequest) {
        if (request.token == currentState.token) return
        loadJob?.cancel()
        setState { TrackActionsState(token = request.token, trackIds = request.trackIds) }
        loadJob = viewModelScope.launch {
            val tracks = (getTracks(request.trackIds) as? NavoResult.Success)?.data.orEmpty()
            if (currentState.token == request.token) setState { copy(isLoading = false, tracks = tracks) }
        }
    }

    private fun enqueue(insertion: QueueInsertion) {
        val state = currentState
        val token = state.token ?: return
        if (state.isWorking) return
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            val effect = when (val result = enqueueTracks(state.trackIds, insertion)) {
                is NavoResult.Success -> when (val outcome = result.data) {
                    EnqueueOutcome.NothingToAdd -> TrackActionsEffect.Failed(token)
                    is EnqueueOutcome.Enqueued -> TrackActionsEffect.Enqueued(token, outcome.insertion, outcome.count)
                    is EnqueueOutcome.StartedPlayback -> TrackActionsEffect.StartedPlayback(token)
                }
                is NavoResult.Error -> TrackActionsEffect.Failed(token)
            }
            if (currentState.token != token) return@launch
            setState { copy(isWorking = false) }
            sendEffect(effect)
        }
    }

    private fun onAddToPlaylistClicked() {
        val state = currentState
        val token = state.token ?: return
        if (state.isWorking) return
        sendEffect(TrackActionsEffect.OpenAddToPlaylist(token, state.trackIds))
    }
}
