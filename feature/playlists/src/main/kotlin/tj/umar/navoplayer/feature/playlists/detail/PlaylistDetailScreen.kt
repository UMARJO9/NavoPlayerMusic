package tj.umar.navoplayer.feature.playlists.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.NavoConfirmDialog
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.NavoSummaryHorizontalPadding
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.component.contentPhase
import tj.umar.navoplayer.core.designsystem.component.navoListPadding
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.core.ui.playlist.PlaylistNameDialog
import tj.umar.navoplayer.core.ui.track.TrackListItem
import tj.umar.navoplayer.feature.playlists.R

@Composable
internal fun PlaylistDetailScreen(
    state: PlaylistDetailState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        TopBar(showMenu = state.playlist != null && !state.isDeleting, onIntent = onIntent)
        val phase = if (state.isMissing) {
            ContentPhase.Empty
        } else {
            contentPhase(hasContent = state.playlist != null, isLoading = state.isLoading, loadFailed = state.loadFailed)
        }
        PhasedContent(
            phase = phase,
            modifier = Modifier.fillMaxSize(),
            empty = {
                StateMessage(
                    message = stringResource(R.string.playlists_missing),
                    palette = MedallionPalettes.all[4],
                    actionLabel = stringResource(R.string.playlists_back),
                    onAction = { onIntent(PlaylistDetailIntent.BackClicked) },
                )
            },
            error = {
                StateMessage(
                    message = stringResource(R.string.playlists_error),
                    palette = MedallionPalettes.all[0],
                    actionLabel = stringResource(R.string.playlists_retry),
                    onAction = { onIntent(PlaylistDetailIntent.RetryLoad) },
                )
            },
        ) {
            val playlist = state.playlist
            if (playlist != null) PlaylistTrackList(state = state, playlist = playlist, onIntent = onIntent)
        }
    }
    PlaylistDialogs(state = state, onIntent = onIntent)
}

@Composable
private fun TopBar(showMenu: Boolean, onIntent: (PlaylistDetailIntent) -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavoIconButton(
            icon = NavoIcons.ChevronLeft,
            contentDescription = stringResource(R.string.playlists_back),
            onClick = { onIntent(PlaylistDetailIntent.BackClicked) },
        )
        Spacer(modifier = Modifier.weight(1f))
        if (showMenu) {
            Box {
                NavoIconButton(
                    icon = NavoIcons.More,
                    contentDescription = stringResource(R.string.playlists_more),
                    onClick = { isMenuOpen = true },
                )
                DropdownMenu(
                    expanded = isMenuOpen,
                    onDismissRequest = { isMenuOpen = false },
                    containerColor = NavoTheme.colors.raised,
                ) {
                    MenuItem(text = stringResource(R.string.playlists_rename)) {
                        isMenuOpen = false
                        onIntent(PlaylistDetailIntent.RenameClicked)
                    }
                    MenuItem(text = stringResource(R.string.playlists_delete)) {
                        isMenuOpen = false
                        onIntent(PlaylistDetailIntent.DeleteClicked)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text = text, style = NavoTheme.typography.label, color = NavoTheme.colors.content) },
        onClick = onClick,
    )
}

@Composable
private fun PlaylistTrackList(
    state: PlaylistDetailState,
    playlist: PlaylistDetail,
    onIntent: (PlaylistDetailIntent) -> Unit,
) {
    val removeLabel = stringResource(R.string.playlists_remove_track_label)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = navoListPadding(state.hasActivePlayback),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "header", contentType = "header") {
            PlaylistHeader(playlist = playlist)
        }
        item(key = "summary", contentType = "summary") {
            PlaylistSummary(state = state, playlist = playlist, onIntent = onIntent)
        }
        items(playlist.tracks, key = { it.id }, contentType = { "track" }) { track ->
            TrackListItem(
                track = track,
                isCurrent = track.id == state.currentTrackId,
                isPlaying = track.id == state.currentTrackId && state.isPlaying,
                onClick = { onIntent(PlaylistDetailIntent.TrackClicked(track.id)) },
                onLongClick = { onIntent(PlaylistDetailIntent.TrackLongPressed(track.id)) },
                onLongClickLabel = removeLabel,
                modifier = Modifier.animateItem(),
            )
        }
        if (playlist.tracks.isEmpty()) {
            item(key = "empty", contentType = "note") {
                EmptyPlaylistNote()
            }
        }
        if (playlist.missingTrackCount > 0) {
            item(key = "missing", contentType = "note") {
                Text(
                    text = pluralStringResource(
                        R.plurals.playlists_unavailable_count,
                        playlist.missingTrackCount,
                        playlist.missingTrackCount,
                    ),
                    style = NavoTheme.typography.caption,
                    color = NavoTheme.colors.contentMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NavoSpacing.Medium, vertical = NavoSpacing.Medium),
                )
            }
        }
    }
}

@Composable
private fun PlaylistHeader(playlist: PlaylistDetail) {
    val palette = remember(playlist.id) { MedallionPalettes.forKey(playlist.id) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.Medium, vertical = NavoSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Medallion(palette = palette, modifier = Modifier.size(104.dp))
        Text(
            text = playlist.name,
            style = NavoTheme.typography.displayM,
            color = NavoTheme.colors.content,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun PlaylistSummary(
    state: PlaylistDetailState,
    playlist: PlaylistDetail,
    onIntent: (PlaylistDetailIntent) -> Unit,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    val count = playlist.tracks.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = NavoSummaryHorizontalPadding, top = 12.dp, end = NavoSummaryHorizontalPadding, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pluralStringResource(CoreUiR.plurals.core_ui_track_count, count, count),
                style = typography.titleS,
                color = colors.content,
            )
            Text(
                text = pluralStringResource(CoreUiR.plurals.core_ui_minute_count, state.totalMinutes, state.totalMinutes),
                style = typography.secondary,
                color = colors.contentSecondary,
            )
        }
        if (count > 0) {
            NavoIconButton(
                icon = NavoIcons.Shuffle,
                contentDescription = stringResource(R.string.playlists_shuffle),
                onClick = { onIntent(PlaylistDetailIntent.ShuffleClicked) },
                containerColor = colors.raised,
                iconSize = 22.dp,
            )
            NavoButton(
                text = stringResource(if (state.isPlaylistPlaying) R.string.playlists_pause else R.string.playlists_play),
                onClick = { onIntent(PlaylistDetailIntent.PlayClicked) },
                leadingIcon = if (state.isPlaylistPlaying) NavoIcons.Pause else NavoIcons.Play,
                height = NavoSpacing.MinTouchTarget,
                contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
                textStyle = typography.label.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun EmptyPlaylistNote() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.Large, vertical = NavoSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.playlists_empty),
            style = NavoTheme.typography.itemTitle,
            color = NavoTheme.colors.content,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.playlists_empty_hint),
            style = NavoTheme.typography.secondary,
            color = NavoTheme.colors.contentSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PlaylistDialogs(state: PlaylistDetailState, onIntent: (PlaylistDetailIntent) -> Unit) {
    val dismiss = { onIntent(PlaylistDetailIntent.DialogDismissed) }
    when (val dialog = state.dialog) {
        is PlaylistDetailDialog.Rename -> PlaylistNameDialog(
            title = stringResource(R.string.playlists_rename_title),
            confirmLabel = stringResource(CoreUiR.string.core_ui_save),
            initialName = dialog.currentName,
            onConfirm = { onIntent(PlaylistDetailIntent.RenameConfirmed(it)) },
            onDismiss = dismiss,
        )
        PlaylistDetailDialog.ConfirmDelete -> NavoConfirmDialog(
            title = stringResource(R.string.playlists_delete_title),
            text = stringResource(R.string.playlists_delete_message, state.playlist?.name.orEmpty()),
            confirmLabel = stringResource(R.string.playlists_delete),
            dismissLabel = stringResource(CoreUiR.string.core_ui_cancel),
            onConfirm = { onIntent(PlaylistDetailIntent.DeleteConfirmed) },
            onDismiss = dismiss,
        )
        is PlaylistDetailDialog.ConfirmRemoveTrack -> NavoConfirmDialog(
            title = stringResource(R.string.playlists_remove_track_title),
            text = stringResource(
                R.string.playlists_remove_track_message,
                dialog.title.ifBlank { stringResource(CoreUiR.string.core_ui_unknown_title) },
            ),
            confirmLabel = stringResource(R.string.playlists_remove),
            dismissLabel = stringResource(CoreUiR.string.core_ui_cancel),
            onConfirm = { onIntent(PlaylistDetailIntent.RemoveTrackConfirmed) },
            onDismiss = dismiss,
        )
        null -> Unit
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PlaylistDetailPreview() {
    NavoTheme {
        PlaylistDetailScreen(
            state = PlaylistDetailState(
                playlistId = previewPlaylist.id,
                isLoading = false,
                playlist = previewPlaylist,
                totalMinutes = 12,
                currentTrackId = 1,
                currentSource = PlaybackSource.Playlist(previewPlaylist.id, previewPlaylist.name),
                isPlaying = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun EmptyPlaylistPreview() {
    NavoTheme {
        PlaylistDetailScreen(
            state = PlaylistDetailState(
                playlistId = 1,
                isLoading = false,
                playlist = PlaylistDetail(id = 1, name = "Новый плейлист", tracks = emptyList(), missingTrackCount = 0),
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 600)
@Composable
private fun MissingPlaylistPreview() {
    NavoTheme {
        PlaylistDetailScreen(
            state = PlaylistDetailState(playlistId = 1, isLoading = false, isMissing = true),
            onIntent = {},
        )
    }
}
