package tj.umar.navoplayer.feature.library.library

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission
import tj.umar.navoplayer.feature.library.R

@Composable
internal fun LibraryRoute(
    onAudioPermissionMissing: () -> Unit,
    onGroupClick: (TrackGroupKey) -> Unit,
    onSearchClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onAddToPlaylist: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)
    val currentOnGroupClick by rememberUpdatedState(onGroupClick)
    val currentOnSearchClick by rememberUpdatedState(onSearchClick)
    val currentOnPlaylistClick by rememberUpdatedState(onPlaylistClick)
    val currentOnAddToPlaylist by rememberUpdatedState(onAddToPlaylist)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(LibraryIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(LibraryIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            LibraryEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            is LibraryEffect.NavigateToGroup -> currentOnGroupClick(effect.key)
            LibraryEffect.NavigateToSearch -> currentOnSearchClick()
            is LibraryEffect.NavigateToPlaylist -> currentOnPlaylistClick(effect.playlistId)
            is LibraryEffect.OpenAddToPlaylist -> currentOnAddToPlaylist(effect.trackIds)
            LibraryEffect.ShowCreatePlaylistFailed ->
                Toast.makeText(context, R.string.library_create_playlist_failed, Toast.LENGTH_SHORT).show()
        }
    }

    LibraryScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
