package tj.umar.navoplayer.feature.library.library

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

@Composable
internal fun LibraryRoute(
    onAudioPermissionMissing: () -> Unit,
    onGroupClick: (TrackGroupKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)
    val currentOnGroupClick by rememberUpdatedState(onGroupClick)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(LibraryIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(LibraryIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            LibraryEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            is LibraryEffect.NavigateToGroup -> currentOnGroupClick(effect.key)
        }
    }

    LibraryScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
