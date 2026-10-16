package tj.umar.navoplayer.feature.player.queue

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.designsystem.component.NavoFullSheet
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.ui.mvi.CollectEffects

private val QueueSheetTopGap = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QueueSheetRoute(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val dismiss = { currentOnDismiss() }
    val activity = LocalActivity.current

    DisposableEffect(viewModel) {
        onDispose {
            if (activity?.isChangingConfigurations != true) viewModel.onIntent(QueueIntent.SheetDismissed)
        }
    }

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(QueueIntent.ScreenStarted)
        onStopOrDispose { viewModel.onIntent(QueueIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            QueueEffect.Close -> scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) dismiss() }
        }
    }

    NavoFullSheet(
        sheetState = sheetState,
        onDismissRequest = dismiss,
        modifier = modifier,
        topGap = QueueSheetTopGap,
        containerColor = NavoTheme.colors.raised,
    ) { contentPadding ->
        QueueScreen(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
    }
}
