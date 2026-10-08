package tj.umar.navoplayer.feature.library.groupdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.grouping.groupFor
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.feature.library.R
import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.GroupArtwork
import tj.umar.navoplayer.feature.library.component.ListSummary
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.ui.track.TrackListItem
import tj.umar.navoplayer.core.designsystem.component.contentPhase
import tj.umar.navoplayer.core.ui.group.description
import tj.umar.navoplayer.core.ui.group.displayTitle
import tj.umar.navoplayer.core.ui.group.leading
import tj.umar.navoplayer.core.designsystem.component.navoListPadding
import tj.umar.navoplayer.feature.library.library.previewTracks

@Composable
internal fun GroupDetailScreen(
    state: GroupDetailState,
    onIntent: (GroupDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            NavoIconButton(
                icon = NavoIcons.ChevronLeft,
                contentDescription = stringResource(R.string.library_back),
                onClick = { onIntent(GroupDetailIntent.BackClicked) },
            )
        }
        val phase = if (state.isMissing) {
            ContentPhase.Empty
        } else {
            contentPhase(hasContent = state.group != null, isLoading = state.isLoading, loadFailed = state.loadFailed)
        }
        PhasedContent(
            phase = phase,
            modifier = Modifier.fillMaxSize(),
            empty = {
                StateMessage(
                    message = stringResource(R.string.library_group_missing),
                    palette = MedallionPalettes.all[4],
                    actionLabel = stringResource(R.string.library_back),
                    onAction = { onIntent(GroupDetailIntent.BackClicked) },
                )
            },
            error = {
                StateMessage(
                    message = stringResource(R.string.library_tracks_error),
                    palette = MedallionPalettes.all[0],
                    actionLabel = stringResource(R.string.library_tracks_retry),
                    onAction = { onIntent(GroupDetailIntent.RetryLoad) },
                )
            },
        ) {
            val group = state.group
            if (group != null) GroupTrackList(state = state, group = group, onIntent = onIntent)
        }
    }
}

@Composable
private fun GroupTrackList(state: GroupDetailState, group: TrackGroup, onIntent: (GroupDetailIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = navoListPadding(state.hasActivePlayback),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "header", contentType = "header") {
            GroupHeader(group = group)
        }
        item(key = "summary", contentType = "summary") {
            ListSummary(
                title = pluralStringResource(CoreUiR.plurals.core_ui_track_count, group.tracks.size, group.tracks.size),
                subtitle = pluralStringResource(CoreUiR.plurals.core_ui_minute_count, state.totalMinutes, state.totalMinutes),
                onSortClick = { onIntent(GroupDetailIntent.SortClicked) },
                onShuffleClick = { onIntent(GroupDetailIntent.ShuffleClicked) },
            )
        }
        items(group.tracks, key = { it.id }, contentType = { "track" }) { track ->
            TrackListItem(
                track = track,
                isCurrent = track.id == state.currentTrackId,
                isPlaying = track.id == state.currentTrackId && state.isPlaying,
                onClick = { onIntent(GroupDetailIntent.TrackClicked(track.id)) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun GroupHeader(group: TrackGroup) {
    val colors = NavoTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.Medium, vertical = NavoSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GroupArtwork(leading = group.key.leading(), size = 104.dp)
        Text(
            text = group.displayTitle(),
            style = NavoTheme.typography.displayM,
            color = colors.content,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        group.description()?.let { description ->
            Text(
                text = description,
                style = NavoTheme.typography.secondary,
                color = colors.contentSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun previewState(key: TrackGroupKey) = GroupDetailState(
    key = key,
    isLoading = false,
    group = previewTracks.groupFor(key),
    totalMinutes = 7,
    currentTrackId = 1,
    isPlaying = true,
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AlbumDetailPreview() {
    NavoTheme {
        GroupDetailScreen(state = previewState(TrackGroupKey(TrackGroupType.Album, 10, null)), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun FolderDetailPreview() {
    NavoTheme {
        GroupDetailScreen(state = previewState(TrackGroupKey(TrackGroupType.Folder, null, "Music/Navo")), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 600)
@Composable
private fun MissingGroupPreview() {
    NavoTheme {
        GroupDetailScreen(
            state = GroupDetailState(key = TrackGroupKey(TrackGroupType.Artist, 7, null), isLoading = false, isMissing = true),
            onIntent = {},
        )
    }
}
