package tj.umar.navoplayer.feature.player.trackactions

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.feature.player.R

@Composable
fun TrackActionsSheetRoute(
    request: TrackActionsRequest,
    onAddToPlaylist: (List<Long>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackActionsSheetRoute(
        request = request,
        onAddToPlaylist = onAddToPlaylist,
        onDismiss = onDismiss,
        modifier = modifier,
        viewModel = hiltViewModel(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackActionsSheetRoute(
    request: TrackActionsRequest,
    onAddToPlaylist: (List<Long>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
    viewModel: TrackActionsViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnAddToPlaylist by rememberUpdatedState(onAddToPlaylist)
    val currentRequest by rememberUpdatedState(request)

    LaunchedEffect(request) {
        viewModel.onIntent(TrackActionsIntent.Opened(request))
    }

    viewModel.effects.CollectEffects { effect ->
        if (effect.token != currentRequest.token) return@CollectEffects
        val message = when (effect) {
            is TrackActionsEffect.Enqueued -> if (effect.insertion == QueueInsertion.Next) {
                R.string.player_queue_will_play_next
            } else {
                R.string.player_queue_added
            }
            is TrackActionsEffect.StartedPlayback -> R.string.player_queue_started
            is TrackActionsEffect.Failed -> R.string.player_queue_failed
            is TrackActionsEffect.OpenAddToPlaylist -> null
        }
        message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (effect is TrackActionsEffect.OpenAddToPlaylist) {
                currentOnAddToPlaylist(effect.trackIds)
            } else {
                currentOnDismiss()
            }
        }
    }

    TrackActionsSheet(
        state = state,
        sheetState = sheetState,
        onIntent = viewModel::onIntent,
        onDismissRequest = { currentOnDismiss() },
        modifier = modifier,
    )
}
