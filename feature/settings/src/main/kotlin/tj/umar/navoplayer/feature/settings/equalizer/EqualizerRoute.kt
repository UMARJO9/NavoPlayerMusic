package tj.umar.navoplayer.feature.settings.equalizer

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
import tj.umar.navoplayer.feature.settings.R

@Composable
internal fun EqualizerRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(EqualizerIntent.ScreenStarted)
        onStopOrDispose { viewModel.onIntent(EqualizerIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            EqualizerEffect.NavigateBack -> currentOnBack()
            EqualizerEffect.ShowSaveFailed ->
                Toast.makeText(context, R.string.settings_save_failed, Toast.LENGTH_SHORT).show()
        }
    }

    EqualizerScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
