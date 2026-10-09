package tj.umar.navoplayer.feature.player.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.mvi.CollectEffects

@Composable
internal fun QueueRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(QueueIntent.ScreenStarted)
        onStopOrDispose { viewModel.onIntent(QueueIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            QueueEffect.Close -> currentOnClose()
        }
    }

    QueueScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
