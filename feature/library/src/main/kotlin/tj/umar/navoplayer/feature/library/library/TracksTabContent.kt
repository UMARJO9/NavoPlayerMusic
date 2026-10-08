package tj.umar.navoplayer.feature.library.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.feature.library.R
import tj.umar.navoplayer.feature.library.component.ListSummary
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.ui.track.TrackListItem
import tj.umar.navoplayer.core.designsystem.component.navoListPadding

@Composable
internal fun TracksTabContent(
    state: LibraryState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LibraryPhasedContent(state = state, onIntent = onIntent, modifier = modifier) {
        TrackList(state = state, onIntent = onIntent)
    }
}

@Composable
internal fun LibraryPhasedContent(
    state: LibraryState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PhasedContent(
        phase = state.contentPhase(),
        modifier = modifier,
        empty = {
            StateMessage(
                message = stringResource(R.string.library_tracks_empty),
                palette = MedallionPalettes.all[4],
            )
        },
        error = {
            StateMessage(
                message = stringResource(R.string.library_tracks_error),
                palette = MedallionPalettes.all[0],
                actionLabel = stringResource(R.string.library_tracks_retry),
                onAction = { onIntent(LibraryIntent.RetryLoadTracks) },
            )
        },
        content = content,
    )
}

@Composable
private fun TrackList(state: LibraryState, onIntent: (LibraryIntent) -> Unit) {
    val addToPlaylistLabel = stringResource(CoreUiR.string.core_ui_add_to_playlist)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = navoListPadding(state.hasActivePlayback),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            ListSummary(
                title = pluralStringResource(CoreUiR.plurals.core_ui_track_count, state.tracks.size, state.tracks.size),
                subtitle = pluralStringResource(CoreUiR.plurals.core_ui_minute_count, state.totalMinutes, state.totalMinutes),
                onSortClick = { onIntent(LibraryIntent.SortClicked) },
                onShuffleClick = { onIntent(LibraryIntent.ShuffleClicked) },
            )
        }
        items(state.tracks, key = { it.id }, contentType = { "track" }) { track ->
            TrackListItem(
                track = track,
                isCurrent = track.id == state.currentTrackId,
                isPlaying = track.id == state.currentTrackId && state.isPlaying,
                onClick = { onIntent(LibraryIntent.TrackClicked(track.id)) },
                onLongClick = { onIntent(LibraryIntent.TrackLongPressed(track.id)) },
                onLongClickLabel = addToPlaylistLabel,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun TracksListPreview() {
    NavoTheme {
        TracksTabContent(
            state = LibraryState(isLoadingTracks = false, tracks = previewTracks, totalMinutes = 73),
            onIntent = {},
            modifier = Modifier.fillMaxSize().background(NavoTheme.colors.background),
        )
    }
}

@Preview(widthDp = 390, heightDp = 600)
@Composable
private fun TracksEmptyPreview() {
    NavoTheme {
        TracksTabContent(
            state = LibraryState(isLoadingTracks = false),
            onIntent = {},
            modifier = Modifier.fillMaxSize().background(NavoTheme.colors.background),
        )
    }
}

@Preview(widthDp = 390, heightDp = 600)
@Composable
private fun TracksErrorPreview() {
    NavoTheme {
        TracksTabContent(
            state = LibraryState(isLoadingTracks = false, tracksLoadFailed = true),
            onIntent = {},
            modifier = Modifier.fillMaxSize().background(NavoTheme.colors.background),
        )
    }
}
