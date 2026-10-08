package tj.umar.navoplayer.feature.playlists.addto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.GroupLeading
import tj.umar.navoplayer.core.designsystem.component.GroupRow
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.model.durationMinutes
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.core.ui.playlist.PlaylistNameForm
import tj.umar.navoplayer.feature.playlists.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddToPlaylistSheet(
    state: AddToPlaylistState,
    sheetState: SheetState,
    onIntent: (AddToPlaylistIntent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = NavoTheme.colors.raised,
        contentColor = NavoTheme.colors.content,
    ) {
        if (state.isNameFormVisible) {
            PlaylistNameForm(
                title = stringResource(CoreUiR.string.core_ui_new_playlist),
                confirmLabel = stringResource(CoreUiR.string.core_ui_create),
                onConfirm = { onIntent(AddToPlaylistIntent.NewPlaylistConfirmed(it)) },
                onCancel = { onIntent(AddToPlaylistIntent.NameFormDismissed) },
            )
        } else {
            AddToPlaylistContent(state = state, onIntent = onIntent)
        }
    }
}

@Composable
internal fun AddToPlaylistContent(state: AddToPlaylistState, onIntent: (AddToPlaylistIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = NavoSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "title", contentType = "title") {
            Text(
                text = stringResource(CoreUiR.string.core_ui_add_to_playlist),
                style = NavoTheme.typography.titleS,
                color = NavoTheme.colors.content,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = NavoSpacing.Small)
                    .semantics { heading() },
            )
        }
        if (state.favoriteTrackId != null) {
            item(key = "favorite", contentType = "action") {
                val isFavorite = state.isFavorite == true
                ActionRow(
                    icon = if (isFavorite) NavoIcons.HeartFilled else NavoIcons.Heart,
                    label = stringResource(
                        if (isFavorite) R.string.playlists_sheet_favorite_remove else R.string.playlists_sheet_favorite_add,
                    ),
                    enabled = !state.isSaving && state.isFavorite != null,
                    onClick = { onIntent(AddToPlaylistIntent.FavoriteClicked) },
                )
            }
        }
        item(key = "new", contentType = "action") {
            ActionRow(
                icon = NavoIcons.Plus,
                label = stringResource(CoreUiR.string.core_ui_new_playlist),
                enabled = !state.isSaving,
                onClick = { onIntent(AddToPlaylistIntent.NewPlaylistClicked) },
            )
        }
        when {
            state.isLoading -> item(key = "loading", contentType = "status") { LoadingRow() }
            state.loadFailed -> item(key = "error", contentType = "status") {
                ErrorRow(onRetry = { onIntent(AddToPlaylistIntent.RetryLoad) })
            }
            else -> items(state.playlists, key = { it.id }, contentType = { "playlist" }) { playlist ->
                GroupRow(
                    title = playlist.name,
                    subtitle = playlist.subtitle(),
                    leading = GroupLeading.Artwork(MedallionPalettes.forKey(playlist.id)),
                    onClick = { onIntent(AddToPlaylistIntent.PlaylistClicked(playlist.id)) },
                )
            }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = NavoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(NavoShapes.TrackRow)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(NavoShapes.Pill)
                .background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.onAccent)
        }
        Text(
            text = label,
            style = NavoTheme.typography.itemTitle,
            color = colors.content,
        )
    }
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(NavoSpacing.Large),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = NavoTheme.colors.accent, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun ErrorRow(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(NavoSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.playlists_sheet_error),
            style = NavoTheme.typography.body,
            color = NavoTheme.colors.contentSecondary,
        )
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.playlists_retry),
                style = NavoTheme.typography.labelStrong,
                color = NavoTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun PlaylistSummary.subtitle(): String {
    val minutes = durationMinutes()
    return stringResource(
        CoreUiR.string.core_ui_group_subtitle,
        pluralStringResource(CoreUiR.plurals.core_ui_track_count, trackCount, trackCount),
        pluralStringResource(CoreUiR.plurals.core_ui_minute_count, minutes, minutes),
    )
}
