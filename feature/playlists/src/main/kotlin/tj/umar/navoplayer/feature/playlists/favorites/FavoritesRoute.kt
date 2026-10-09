package tj.umar.navoplayer.feature.playlists.favorites

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission
import tj.umar.navoplayer.feature.playlists.R

@Composable
internal fun FavoritesRoute(
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(FavoritesIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(FavoritesIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            FavoritesEffect.NavigateBack -> currentOnBack()
            FavoritesEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            is FavoritesEffect.TrackRemoved -> {
                val title = effect.title.ifBlank { resources.getString(CoreUiR.string.core_ui_unknown_title) }
                Toast.makeText(context, resources.getString(R.string.playlists_favorites_removed, title), Toast.LENGTH_SHORT).show()
            }
            FavoritesEffect.RemoveFailed ->
                Toast.makeText(context, R.string.playlists_favorites_remove_failed, Toast.LENGTH_SHORT).show()
        }
    }

    FavoritesScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
