package tj.umar.navoplayer.feature.library.library

import android.app.Activity
import android.content.ActivityNotFoundException
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.core.ui.permission.audioReadPermission
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission

@Composable
internal fun LibraryRoute(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val rationaleBeforeRequest = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onIntent(
            LibraryIntent.PermissionResult(
                granted = granted,
                rationaleBefore = rationaleBeforeRequest.value,
                rationaleAfter = activity.shouldShowAudioRationale(),
            ),
        )
    }

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(LibraryIntent.PermissionChecked(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(LibraryIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            LibraryEffect.RequestAudioPermission -> {
                rationaleBeforeRequest.value = activity.shouldShowAudioRationale()
                permissionLauncher.launch(audioReadPermission)
            }
            LibraryEffect.OpenAppSettings -> context.openAppSettings()
        }
    }

    LibraryScreen(
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
    try {
        startActivity(appDetails)
    } catch (e: ActivityNotFoundException) {
        runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }
}
