package tj.umar.navoplayer.feature.library.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.ui.format.formatDuration
import tj.umar.navoplayer.feature.library.R

@Composable
internal fun TracksTabContent(
    state: LibraryState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GrantedTracksContent(
        state = state,
        onRetryClick = { onIntent(LibraryIntent.RetryLoadTracks) },
        modifier = modifier,
    )
}

@Composable
private fun GrantedTracksContent(
    state: LibraryState,
    onRetryClick: () -> Unit,
    modifier: Modifier,
) {
    when {
        state.tracks.isNotEmpty() -> TrackList(state.tracks, modifier)
        state.isLoadingTracks -> CenteredBox(modifier) {
            CircularProgressIndicator()
        }
        state.tracksLoadFailed -> MessageWithAction(
            message = stringResource(R.string.library_tracks_error),
            actionLabel = stringResource(R.string.library_tracks_retry),
            onActionClick = onRetryClick,
            modifier = modifier,
        )
        else -> CenteredMessage(stringResource(R.string.library_tracks_empty), modifier)
    }
}

@Composable
private fun TrackList(tracks: List<Track>, modifier: Modifier) {
    LazyColumn(modifier = modifier) {
        items(tracks, key = { it.id }) { track ->
            TrackRow(
                title = track.title,
                artist = track.artist,
                durationMs = track.durationMs,
            )
        }
    }
}

@Composable
private fun TrackRow(
    title: String,
    artist: String?,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier,
        headlineContent = {
            Text(
                text = title.ifBlank { stringResource(R.string.library_unknown_title) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = artist ?: stringResource(R.string.library_unknown_artist),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelMedium,
            )
        },
    )
}

@Composable
private fun MessageWithAction(
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = NavoSpacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(NavoSpacing.Medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onActionClick) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun CenteredMessage(text: String, modifier: Modifier) {
    CenteredBox(modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = NavoSpacing.ExtraLarge),
        )
    }
}

@Composable
private fun CenteredBox(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        content()
    }
}

internal val previewTracks = listOf(
    Track(1, "Alpha", "Navo Band", "First", 10, 100, 185_000, 1, "content://media/1"),
    Track(2, "", null, null, null, null, 42_000, null, "content://media/2"),
    Track(3, "Long Mix", "DJ Navo", "Mixes", 11, 101, 3_725_000, 2, "content://media/3"),
)

@PreviewLightDark
@Composable
private fun TracksListPreview() {
    NavoTheme {
        TracksTabContent(
            state = LibraryState(tracks = previewTracks),
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@PreviewLightDark
@Composable
private fun TracksLoadingPreview() {
    NavoTheme {
        TracksTabContent(
            state = LibraryState(isLoadingTracks = true),
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@PreviewLightDark
@Composable
private fun TracksEmptyPreview() {
    NavoTheme {
        TracksTabContent(
            state = LibraryState(isLoadingTracks = false),
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@PreviewLightDark
@Composable
private fun TracksErrorPreview() {
    NavoTheme {
        TracksTabContent(
            state = LibraryState(tracksLoadFailed = true),
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
