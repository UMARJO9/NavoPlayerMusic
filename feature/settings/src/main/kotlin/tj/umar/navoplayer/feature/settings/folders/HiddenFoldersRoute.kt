package tj.umar.navoplayer.feature.settings.folders

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
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission
import tj.umar.navoplayer.feature.settings.R

@Composable
internal fun HiddenFoldersRoute(
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HiddenFoldersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(HiddenFoldersIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(HiddenFoldersIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            HiddenFoldersEffect.NavigateBack -> currentOnBack()
            HiddenFoldersEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            HiddenFoldersEffect.ShowSaveFailed ->
                Toast.makeText(context, R.string.settings_save_failed, Toast.LENGTH_SHORT).show()
        }
    }

    HiddenFoldersScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
