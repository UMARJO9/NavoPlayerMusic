package tj.umar.navoplayer.feature.playlists.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission
import tj.umar.navoplayer.feature.playlists.R
import tj.umar.navoplayer.feature.playlists.component.UndoSnackbarHost
import tj.umar.navoplayer.feature.playlists.component.showUndoSnackbar
import tj.umar.navoplayer.core.ui.R as CoreUiR

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
    val resources = LocalResources.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val undoLabel = stringResource(R.string.playlists_undo)
    val unknownTitle = stringResource(CoreUiR.string.core_ui_unknown_title)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(PlaylistDetailIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(PlaylistDetailIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            PlaylistDetailEffect.NavigateBack -> currentOnBack()
            PlaylistDetailEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            is PlaylistDetailEffect.ShowMessage -> Toast.makeText(context, effect.message.textRes(), Toast.LENGTH_SHORT).show()
            is PlaylistDetailEffect.ShowRemoveUndo -> snackbarScope.showUndoSnackbar(
                hostState = snackbarHostState,
                message = resources.getString(R.string.playlists_track_removed, effect.title.ifBlank { unknownTitle }),
                actionLabel = undoLabel,
            ) { undo ->
                viewModel.onIntent(
                    if (undo) {
                        PlaylistDetailIntent.UndoRemoveClicked(effect.token)
                    } else {
                        PlaylistDetailIntent.UndoRemoveDismissed(effect.token)
                    },
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PlaylistDetailScreen(
            state = state,
            onIntent = viewModel::onIntent,
        )
        UndoSnackbarHost(
            hostState = snackbarHostState,
            hasActivePlayback = state.hasActivePlayback,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun PlaylistDetailMessage.textRes(): Int = when (this) {
    PlaylistDetailMessage.RenameFailed -> R.string.playlists_rename_failed
    PlaylistDetailMessage.DeleteFailed -> R.string.playlists_delete_failed
    PlaylistDetailMessage.RemoveFailed -> R.string.playlists_remove_failed
    PlaylistDetailMessage.RestoreFailed -> R.string.playlists_restore_failed
    PlaylistDetailMessage.ReorderFailed -> R.string.playlists_reorder_failed
}
