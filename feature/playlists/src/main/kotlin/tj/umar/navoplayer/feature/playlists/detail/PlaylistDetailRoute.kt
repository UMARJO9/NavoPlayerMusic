package tj.umar.navoplayer.feature.playlists.detail

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
import tj.umar.navoplayer.feature.playlists.R

@Composable
internal fun PlaylistDetailRoute(
    playlistId: Long,
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel<PlaylistDetailViewModel, PlaylistDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(playlistId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(PlaylistDetailIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(PlaylistDetailIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            PlaylistDetailEffect.NavigateBack -> currentOnBack()
            PlaylistDetailEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            is PlaylistDetailEffect.ShowMessage -> Toast.makeText(context, effect.message.textRes(), Toast.LENGTH_SHORT).show()
        }
    }

    PlaylistDetailScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

private fun PlaylistDetailMessage.textRes(): Int = when (this) {
    PlaylistDetailMessage.RenameFailed -> R.string.playlists_rename_failed
    PlaylistDetailMessage.DeleteFailed -> R.string.playlists_delete_failed
    PlaylistDetailMessage.RemoveFailed -> R.string.playlists_remove_failed
}
