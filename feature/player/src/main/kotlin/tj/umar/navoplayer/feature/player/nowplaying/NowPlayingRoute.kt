package tj.umar.navoplayer.feature.player.nowplaying

import android.content.res.Resources
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.feature.player.R

@Composable
internal fun NowPlayingRoute(
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnCollapse by rememberUpdatedState(onCollapse)
    val currentOnOpenQueue by rememberUpdatedState(onOpenQueue)
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(NowPlayingIntent.ScreenStarted)
        onStopOrDispose { viewModel.onIntent(NowPlayingIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            NowPlayingEffect.Collapse -> currentOnCollapse()
            NowPlayingEffect.OpenQueue -> currentOnOpenQueue()
            is NowPlayingEffect.ShowMessage ->
                Toast.makeText(context, effect.message.text(context.resources), Toast.LENGTH_SHORT).show()
        }
    }

    NowPlayingScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

private fun NowPlayingMessage.text(resources: Resources): String = when (this) {
    NowPlayingMessage.FavoriteFailed -> resources.getString(R.string.player_favorite_failed)
    is NowPlayingMessage.SleepTimerSet ->
        resources.getQuantityString(R.plurals.player_sleep_timer_set_minutes, minutes, minutes)
    NowPlayingMessage.SleepTimerEndOfTrack -> resources.getString(R.string.player_sleep_timer_set_end_of_track)
    NowPlayingMessage.SleepTimerOff -> resources.getString(R.string.player_sleep_timer_off)
    NowPlayingMessage.SleepTimerUnavailable -> resources.getString(R.string.player_sleep_timer_unavailable)
}
