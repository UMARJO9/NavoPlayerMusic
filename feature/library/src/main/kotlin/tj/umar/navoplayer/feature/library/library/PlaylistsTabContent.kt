package tj.umar.navoplayer.feature.library.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.GroupLeading
import tj.umar.navoplayer.core.designsystem.component.GroupRow
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.NavoSummaryHorizontalPadding
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.component.contentPhase
import tj.umar.navoplayer.core.designsystem.component.navoListPadding
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.model.durationMinutes
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.core.ui.playlist.PlaylistNameSheet
import tj.umar.navoplayer.feature.library.R

@Composable
internal fun PlaylistsTabContent(state: LibraryState, onIntent: (LibraryIntent) -> Unit, modifier: Modifier = Modifier) {
    PhasedContent(
        phase = contentPhase(
            hasContent = !state.isLoadingPlaylists && !state.playlistsLoadFailed,
            isLoading = state.isLoadingPlaylists,
            loadFailed = state.playlistsLoadFailed,
        ),
        modifier = modifier,
        empty = {},
        error = {
            StateMessage(
                message = stringResource(R.string.library_playlists_error),
                palette = MedallionPalettes.all[0],
                actionLabel = stringResource(R.string.library_tracks_retry),
                onAction = { onIntent(LibraryIntent.RetryLoadPlaylists) },
            )
        },
    ) {
        PlaylistList(state = state, onIntent = onIntent)
    }
    if (state.isCreatePlaylistSheetVisible) {
        PlaylistNameSheet(
            title = stringResource(CoreUiR.string.core_ui_new_playlist),
            confirmLabel = stringResource(CoreUiR.string.core_ui_create),
            onConfirm = { onIntent(LibraryIntent.CreatePlaylistConfirmed(it)) },
            onDismiss = { onIntent(LibraryIntent.CreatePlaylistDismissed) },
        )
    }
}

@Composable
private fun PlaylistList(state: LibraryState, onIntent: (LibraryIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = navoListPadding(state.hasActivePlayback),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            PlaylistsSummary(count = state.playlists.size, onIntent = onIntent)
        }
        item(key = "favorites", contentType = "playlist") {
            val minutes = state.favorites.durationMinutes()
            GroupRow(
                title = stringResource(CoreUiR.string.core_ui_favorites),
                subtitle = stringResource(
                    CoreUiR.string.core_ui_group_subtitle,
                    pluralStringResource(CoreUiR.plurals.core_ui_track_count, state.favorites.trackCount, state.favorites.trackCount),
                    pluralStringResource(CoreUiR.plurals.core_ui_minute_count, minutes, minutes),
                ),
                leading = GroupLeading.Favorites,
                onClick = { onIntent(LibraryIntent.FavoritesClicked) },
            )
        }
        if (state.playlists.isEmpty()) {
            item(key = "empty", contentType = "note") {
                Text(
                    text = stringResource(R.string.library_playlists_empty),
                    style = NavoTheme.typography.secondary,
                    color = NavoTheme.colors.contentSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NavoSpacing.Large),
                )
            }
        }
        items(state.playlists, key = { it.id }, contentType = { "playlist" }) { playlist ->
            GroupRow(
                title = playlist.name,
                subtitle = playlist.subtitle(),
                leading = GroupLeading.Artwork(MedallionPalettes.forKey(playlist.id)),
                onClick = { onIntent(LibraryIntent.PlaylistClicked(playlist.id)) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun PlaylistsSummary(count: Int, onIntent: (LibraryIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = NavoSummaryHorizontalPadding, top = 12.dp, end = NavoSummaryHorizontalPadding, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pluralStringResource(R.plurals.library_playlist_count, count, count),
            style = NavoTheme.typography.titleS,
            color = NavoTheme.colors.content,
            modifier = Modifier.weight(1f),
        )
        NavoButton(
            text = stringResource(CoreUiR.string.core_ui_new_playlist),
            onClick = { onIntent(LibraryIntent.CreatePlaylistClicked) },
            leadingIcon = NavoIcons.Plus,
            height = NavoSpacing.MinTouchTarget,
            contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
            textStyle = NavoTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
        )
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

private val previewPlaylists = listOf(
    PlaylistSummary(id = 1, name = "Утро в горах", trackCount = 12, durationMs = 2_940_000),
    PlaylistSummary(id = 2, name = "Дорога", trackCount = 1, durationMs = 185_000),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PlaylistsTabPreview() {
    NavoTheme {
        PlaylistsTabContent(
            state = LibraryState(isLoadingPlaylists = false, playlists = previewPlaylists),
            onIntent = {},
            modifier = Modifier.fillMaxSize().background(NavoTheme.colors.background),
        )
    }
}

@Preview(widthDp = 390, heightDp = 600)
@Composable
private fun EmptyPlaylistsTabPreview() {
    NavoTheme {
        PlaylistsTabContent(
            state = LibraryState(isLoadingPlaylists = false),
            onIntent = {},
            modifier = Modifier.fillMaxSize().background(NavoTheme.colors.background),
        )
    }
}
