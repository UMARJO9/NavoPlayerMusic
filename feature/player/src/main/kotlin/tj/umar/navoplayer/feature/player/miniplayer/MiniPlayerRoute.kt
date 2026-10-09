package tj.umar.navoplayer.feature.player.miniplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.designsystem.medallion.rememberMedallionRotation
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.ui.mvi.CollectEffects

private const val ENTER_MILLIS = 280
private const val EXIT_MILLIS = 200
private const val ROTATION_FRAME_MILLIS = 33L

@Composable
fun MiniPlayerRoute(
    visible: Boolean,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MiniPlayerRoute(visible = visible, onOpenNowPlaying = onOpenNowPlaying, modifier = modifier, viewModel = hiltViewModel())
}

@Composable
internal fun MiniPlayerRoute(
    visible: Boolean,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier,
    viewModel: MiniPlayerViewModel,
) {
    val stateHolder = viewModel.state.collectAsStateWithLifecycle()
    val track by remember { derivedStateOf { stateHolder.value.track } }
    val isPlaying by remember { derivedStateOf { stateHolder.value.isPlaying } }
    val currentOnOpenNowPlaying by rememberUpdatedState(onOpenNowPlaying)
    var lastTrack by remember { mutableStateOf<Track?>(null) }
    val rotation = rememberMedallionRotation(running = isPlaying && visible, frameIntervalMillis = ROTATION_FRAME_MILLIS)

    LifecycleStartEffect(visible) {
        if (visible) viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        onStopOrDispose { if (visible) viewModel.onIntent(MiniPlayerIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            MiniPlayerEffect.OpenNowPlaying -> currentOnOpenNowPlaying()
        }
    }

    val currentTrack = track
    SideEffect {
        if (currentTrack != null) lastTrack = currentTrack
    }

    AnimatedVisibility(
        visible = visible && currentTrack != null,
        modifier = modifier,
        enter = slideInVertically(tween(ENTER_MILLIS)) { it } + fadeIn(tween(ENTER_MILLIS)),
        exit = slideOutVertically(tween(EXIT_MILLIS)) { it } + fadeOut(tween(EXIT_MILLIS)),
    ) {
        val shownTrack = currentTrack ?: lastTrack ?: return@AnimatedVisibility
        MiniPlayer(
            track = shownTrack,
            isPlaying = isPlaying,
            progress = { stateHolder.value.progress },
            rotation = rotation,
            onIntent = viewModel::onIntent,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        )
    }
}
