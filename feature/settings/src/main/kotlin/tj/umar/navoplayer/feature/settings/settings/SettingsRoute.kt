package tj.umar.navoplayer.feature.settings.settings

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
internal fun SettingsRoute(
    onBack: () -> Unit,
    onHiddenFoldersClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnHiddenFoldersClick by rememberUpdatedState(onHiddenFoldersClick)
    val currentOnLicensesClick by rememberUpdatedState(onLicensesClick)
    val currentOnEqualizerClick by rememberUpdatedState(onEqualizerClick)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(SettingsIntent.ScreenStarted)
        onStopOrDispose { viewModel.onIntent(SettingsIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            SettingsEffect.NavigateBack -> currentOnBack()
            SettingsEffect.NavigateToHiddenFolders -> currentOnHiddenFoldersClick()
            SettingsEffect.NavigateToLicenses -> currentOnLicensesClick()
            SettingsEffect.NavigateToEqualizer -> currentOnEqualizerClick()
            SettingsEffect.ShowSaveFailed ->
                Toast.makeText(context, R.string.settings_save_failed, Toast.LENGTH_SHORT).show()
        }
    }

    SettingsScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
