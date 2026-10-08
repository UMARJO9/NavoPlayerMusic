package tj.umar.navoplayer.feature.welcome.welcome

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.core.ui.permission.audioReadPermission
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission

@Composable
internal fun WelcomeRoute(
    onPermissionGranted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnPermissionGranted by rememberUpdatedState(onPermissionGranted)

    val rationaleBeforeRequest = rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onIntent(
            WelcomeIntent.PermissionResult(
                granted = granted,
                rationaleBefore = rationaleBeforeRequest.value,
                rationaleAfter = activity.shouldShowAudioRationale(),
            ),
        )
    }

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(
            WelcomeIntent.ScreenStarted(
                granted = context.hasAudioReadPermission(),
                rationaleShown = activity.shouldShowAudioRationale(),
            ),
        )
        onStopOrDispose { }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            WelcomeEffect.RequestAudioPermission -> {
                rationaleBeforeRequest.value = activity.shouldShowAudioRationale()
                permissionLauncher.launch(audioReadPermission)
            }
            WelcomeEffect.OpenAppSettings -> context.openAppSettings()
            WelcomeEffect.NavigateToLibrary -> currentOnPermissionGranted()
        }
    }

    WelcomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

private fun Activity?.shouldShowAudioRationale(): Boolean =
    this?.shouldShowRequestPermissionRationale(audioReadPermission) ?: false

private fun Context.openAppSettings() {
    val appDetails = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
    runCatching { startActivity(appDetails) }
        .onFailure { runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) } }
}
