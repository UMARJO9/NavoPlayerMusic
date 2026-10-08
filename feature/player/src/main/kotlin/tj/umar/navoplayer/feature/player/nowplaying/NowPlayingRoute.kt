package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.mvi.CollectEffects

@Composable
internal fun NowPlayingRoute(
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnCollapse by rememberUpdatedState(onCollapse)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(NowPlayingIntent.ScreenStarted)
        onStopOrDispose { viewModel.onIntent(NowPlayingIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            NowPlayingEffect.Collapse -> currentOnCollapse()
        }
    }

    NowPlayingScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
