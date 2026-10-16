package tj.umar.navoplayer.feature.player.nowplaying

import android.content.res.Resources
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.designsystem.component.NavoFullSheet
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.feature.player.R
import tj.umar.navoplayer.feature.player.queue.QueueSheetRoute

@Composable
fun NowPlayingSheetRoute(
    onDismiss: () -> Unit,
    onOpenTrackActions: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    NowPlayingSheetRoute(
        onDismiss = onDismiss,
        onOpenTrackActions = onOpenTrackActions,
        modifier = modifier,
        viewModel = hiltViewModel(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NowPlayingSheetRoute(
    onDismiss: () -> Unit,
    onOpenTrackActions: (List<Long>) -> Unit,
    modifier: Modifier,
    viewModel: NowPlayingViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnOpenTrackActions by rememberUpdatedState(onOpenTrackActions)
    val context = LocalContext.current
    val dismiss = { currentOnDismiss() }
    val activity = LocalActivity.current

    DisposableEffect(viewModel) {
        onDispose {
            if (activity?.isChangingConfigurations != true) viewModel.onIntent(NowPlayingIntent.SheetDismissed)
        }
    }

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(NowPlayingIntent.ScreenStarted)
        onStopOrDispose { viewModel.onIntent(NowPlayingIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            NowPlayingEffect.Collapse -> scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) dismiss() }
            is NowPlayingEffect.OpenTrackActions -> currentOnOpenTrackActions(effect.trackIds)
            is NowPlayingEffect.ShowMessage ->
                Toast.makeText(context, effect.message.text(context.resources), Toast.LENGTH_SHORT).show()
        }
    }

    NavoFullSheet(
        sheetState = sheetState,
        onDismissRequest = dismiss,
        modifier = modifier,
    ) {
        NowPlayingScreen(state = state, onIntent = viewModel::onIntent)
        if (state.isSleepTimerSheetVisible) {
            SleepTimerSheet(sleepTimer = state.sleepTimer, onIntent = viewModel::onIntent)
        }
        if (state.isQueueSheetVisible) {
            QueueSheetRoute(onDismiss = { viewModel.onIntent(NowPlayingIntent.QueueSheetDismissed) })
        }
    }
}

private fun NowPlayingMessage.text(resources: Resources): String = when (this) {
    NowPlayingMessage.FavoriteFailed -> resources.getString(R.string.player_favorite_failed)
    is NowPlayingMessage.SleepTimerSet ->
        resources.getQuantityString(R.plurals.player_sleep_timer_set_minutes, minutes, minutes)
    NowPlayingMessage.SleepTimerEndOfTrack -> resources.getString(R.string.player_sleep_timer_set_end_of_track)
    NowPlayingMessage.SleepTimerOff -> resources.getString(R.string.player_sleep_timer_off)
    NowPlayingMessage.SleepTimerUnavailable -> resources.getString(R.string.player_sleep_timer_unavailable)
}
