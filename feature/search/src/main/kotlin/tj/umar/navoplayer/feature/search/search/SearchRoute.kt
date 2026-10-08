package tj.umar.navoplayer.feature.search.search

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
internal fun SearchRoute(
    onBack: () -> Unit,
    onGroupClick: (TrackGroupKey) -> Unit,
    onAudioPermissionMissing: () -> Unit,
    onAddToPlaylist: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnGroupClick by rememberUpdatedState(onGroupClick)
    val currentOnAudioPermissionMissing by rememberUpdatedState(onAudioPermissionMissing)
    val currentOnAddToPlaylist by rememberUpdatedState(onAddToPlaylist)

    LifecycleStartEffect(Unit) {
        viewModel.onIntent(SearchIntent.ScreenStarted(context.hasAudioReadPermission()))
        onStopOrDispose { viewModel.onIntent(SearchIntent.ScreenStopped) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            SearchEffect.NavigateBack -> currentOnBack()
            SearchEffect.NavigateToWelcome -> currentOnAudioPermissionMissing()
            is SearchEffect.NavigateToGroup -> currentOnGroupClick(effect.key)
            is SearchEffect.OpenAddToPlaylist -> currentOnAddToPlaylist(effect.trackIds)
        }
    }

    SearchScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
