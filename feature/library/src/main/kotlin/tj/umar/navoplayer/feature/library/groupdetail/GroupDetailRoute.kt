package tj.umar.navoplayer.feature.library.groupdetail

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
internal fun GroupDetailRoute(
    key: TrackGroupKey,
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
    onTrackActions: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = hiltViewModel<GroupDetailViewModel, GroupDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(key) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)
    val currentOnTrackActions by rememberUpdatedState(onTrackActions)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(GroupDetailIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(GroupDetailIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            GroupDetailEffect.NavigateBack -> currentOnBack()
            GroupDetailEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            is GroupDetailEffect.OpenTrackActions -> currentOnTrackActions(effect.trackIds)
        }
    }

    GroupDetailScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
