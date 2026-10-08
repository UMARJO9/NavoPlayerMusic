package tj.umar.navoplayer.feature.library.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.grouping.toAlbums
import tj.umar.navoplayer.core.domain.grouping.toArtists
import tj.umar.navoplayer.core.domain.grouping.toFolders
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.feature.library.R
import tj.umar.navoplayer.core.designsystem.component.GroupRow
import tj.umar.navoplayer.feature.library.component.ListSummary
import tj.umar.navoplayer.core.ui.group.displaySubtitle
import tj.umar.navoplayer.core.ui.group.displayTitle
import tj.umar.navoplayer.core.ui.group.leading
import tj.umar.navoplayer.core.designsystem.component.navoListPadding
import tj.umar.navoplayer.core.ui.group.stableKey

@Composable
internal fun AlbumsTabContent(state: LibraryState, onIntent: (LibraryIntent) -> Unit, modifier: Modifier = Modifier) {
    GroupTabContent(state, state.albums, CoreUiR.plurals.core_ui_album_count, onIntent, modifier)
}

@Composable
internal fun ArtistsTabContent(state: LibraryState, onIntent: (LibraryIntent) -> Unit, modifier: Modifier = Modifier) {
    GroupTabContent(state, state.artists, R.plurals.library_artist_count, onIntent, modifier)
}

@Composable
internal fun FoldersTabContent(state: LibraryState, onIntent: (LibraryIntent) -> Unit, modifier: Modifier = Modifier) {
    GroupTabContent(state, state.folders, R.plurals.library_folder_count, onIntent, modifier)
}

@Composable
private fun GroupTabContent(
    state: LibraryState,
    groups: List<TrackGroup>,
    countPluralRes: Int,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier,
) {
    LibraryPhasedContent(state = state, onIntent = onIntent, modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = navoListPadding(state.hasActivePlayback),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item(key = "summary", contentType = "summary") {
                ListSummary(
                    title = pluralStringResource(countPluralRes, groups.size, groups.size),
                    subtitle = pluralStringResource(CoreUiR.plurals.core_ui_track_count, state.tracks.size, state.tracks.size),
                    onSortClick = { onIntent(LibraryIntent.SortClicked) },
                    onShuffleClick = { onIntent(LibraryIntent.ShuffleClicked) },
                )
            }
            items(groups, key = { it.key.stableKey() }, contentType = { "group" }) { group ->
                GroupRow(
                    title = group.displayTitle(),
                    subtitle = group.displaySubtitle(),
                    leading = group.key.leading(),
                    onClick = { onIntent(LibraryIntent.GroupClicked(group.key)) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

internal val previewLibraryState = LibraryState(
    isLoadingTracks = false,
    tracks = previewTracks,
    albums = previewTracks.toAlbums(),
    artists = previewTracks.toArtists(),
    folders = previewTracks.toFolders(),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AlbumsTabPreview() {
    NavoTheme {
        AlbumsTabContent(previewLibraryState, {}, Modifier.fillMaxSize().background(NavoTheme.colors.background))
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ArtistsTabPreview() {
    NavoTheme {
        ArtistsTabContent(previewLibraryState, {}, Modifier.fillMaxSize().background(NavoTheme.colors.background))
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun FoldersTabPreview() {
    NavoTheme {
        FoldersTabContent(previewLibraryState, {}, Modifier.fillMaxSize().background(NavoTheme.colors.background))
    }
}
