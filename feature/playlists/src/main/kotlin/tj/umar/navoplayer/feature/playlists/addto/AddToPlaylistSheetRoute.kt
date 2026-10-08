package tj.umar.navoplayer.feature.playlists.addto

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.ui.mvi.CollectEffects
import tj.umar.navoplayer.feature.playlists.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheetRoute(
    trackIds: List<Long>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AddToPlaylistSheetRoute(trackIds = trackIds, onDismiss = onDismiss, modifier = modifier, viewModel = hiltViewModel())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddToPlaylistSheetRoute(
    trackIds: List<Long>,
    onDismiss: () -> Unit,
    modifier: Modifier,
    viewModel: AddToPlaylistViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(trackIds) {
        viewModel.onIntent(AddToPlaylistIntent.Opened(trackIds))
    }
    DisposableEffect(viewModel) {
        onDispose { viewModel.onIntent(AddToPlaylistIntent.Dismissed) }
    }

    viewModel.effects.CollectEffects { effect ->
        Toast.makeText(context, effect.message(context), Toast.LENGTH_SHORT).show()
        scope.launch { sheetState.hide() }.invokeOnCompletion { currentOnDismiss() }
    }

    AddToPlaylistSheet(
        state = state,
        sheetState = sheetState,
        onIntent = viewModel::onIntent,
        onDismissRequest = { currentOnDismiss() },
        modifier = modifier,
    )
}

private fun AddToPlaylistEffect.message(context: Context): String = when (this) {
    is AddToPlaylistEffect.Added -> context.getString(
        if (addedCount == 0) R.string.playlists_sheet_already_added else R.string.playlists_sheet_added,
        playlistName,
    )
    is AddToPlaylistEffect.Created -> context.getString(R.string.playlists_sheet_created, playlistName)
    AddToPlaylistEffect.Failed -> context.getString(R.string.playlists_sheet_failed)
}

@Preview(widthDp = 390)
@Composable
private fun AddToPlaylistContentPreview() {
    NavoTheme {
        Box(modifier = Modifier.background(NavoTheme.colors.raised)) {
            AddToPlaylistContent(
                state = AddToPlaylistState(
                    trackIds = listOf(1),
                    isLoading = false,
                    playlists = listOf(
                        PlaylistSummary(id = 1, name = "Утро в горах", trackCount = 12, durationMs = 2_940_000),
                        PlaylistSummary(id = 2, name = "Дорога", trackCount = 3, durationMs = 640_000),
                    ),
                ),
                onIntent = {},
            )
        }
    }
}
