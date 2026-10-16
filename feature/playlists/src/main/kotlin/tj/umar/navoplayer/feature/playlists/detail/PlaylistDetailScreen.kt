package tj.umar.navoplayer.feature.playlists.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.animatedListItem
import tj.umar.navoplayer.core.designsystem.animation.rememberListEntrance
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.NavoConfirmDialog
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.component.contentPhase
import tj.umar.navoplayer.core.designsystem.component.navoListPadding
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.core.ui.playlist.PlaylistNameSheet
import tj.umar.navoplayer.core.ui.track.TrackListItem
import tj.umar.navoplayer.feature.playlists.R
import tj.umar.navoplayer.feature.playlists.component.CollapsedTitle
import tj.umar.navoplayer.feature.playlists.component.CollectionEmptyNote
import tj.umar.navoplayer.feature.playlists.component.CollectionHeader
import tj.umar.navoplayer.feature.playlists.component.CollectionSummary
import tj.umar.navoplayer.feature.playlists.component.HEADER_ITEM_KEY
import tj.umar.navoplayer.feature.playlists.component.UnavailableTracksNote
import tj.umar.navoplayer.feature.playlists.component.headerCollapseFraction

private const val SETTLE_START_DEGREES = -18f
private const val SETTLE_MILLIS = 600
private val SettleEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

@Composable
internal fun PlaylistDetailScreen(
    state: PlaylistDetailState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val collapse = remember(listState) { { listState.headerCollapseFraction() } }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        TopBar(
            title = state.playlist?.name.orEmpty(),
            collapse = collapse,
            showMenu = state.playlist != null && !state.isDeleting,
            onIntent = onIntent,
        )
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
            if (playlist != null) {
                PlaylistTrackList(state = state, playlist = playlist, listState = listState, collapse = collapse, onIntent = onIntent)
            }
        }
    }
    PlaylistDialogs(state = state, onIntent = onIntent)
}

@Composable
private fun TopBar(
    title: String,
    collapse: () -> Float,
    showMenu: Boolean,
    onIntent: (PlaylistDetailIntent) -> Unit,
) {
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
        CollapsedTitle(
            title = title,
            collapse = collapse,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
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
        } else {
            Spacer(modifier = Modifier.size(44.dp))
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
    listState: LazyListState,
    collapse: () -> Float,
    onIntent: (PlaylistDetailIntent) -> Unit,
) {
    val removeLabel = stringResource(R.string.playlists_remove_track_label)
    val motion = rememberMotionEnabled()
    val entrance = rememberListEntrance()
    val settle = rememberMedallionSettle(key = playlist.id, motion = motion)
    val trackCount = state.tracks.size
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = navoListPadding(state.hasActivePlayback),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = HEADER_ITEM_KEY, contentType = "header") {
            CollectionHeader(
                title = playlist.name,
                collapse = collapse,
                modifier = Modifier.animatedListItem(this, 0, motion, entrance),
            ) {
                val palette = remember(playlist.id) { MedallionPalettes.forKey(playlist.id) }
                Medallion(palette = palette, modifier = Modifier.size(104.dp), rotationDegrees = settle)
            }
        }
        item(key = "summary", contentType = "summary") {
            CollectionSummary(
                trackCount = trackCount,
                totalMinutes = state.totalMinutes,
                isPlaying = state.isPlaylistPlaying,
                onPlay = { onIntent(PlaylistDetailIntent.PlayClicked) },
                onShuffle = { onIntent(PlaylistDetailIntent.ShuffleClicked) },
                modifier = Modifier.animatedListItem(this, 1, motion, entrance),
            )
        }
        itemsIndexed(state.tracks, key = { _, track -> track.id }, contentType = { _, _ -> "track" }) { index, track ->
            TrackListItem(
                track = track,
                isCurrent = track.id == state.currentTrackId,
                isPlaying = track.id == state.currentTrackId && state.isPlaying,
                onClick = { onIntent(PlaylistDetailIntent.TrackClicked(track.id)) },
                onLongClick = { onIntent(PlaylistDetailIntent.TrackLongPressed(track.id)) },
                onLongClickLabel = removeLabel,
                modifier = Modifier.animatedListItem(this, 2 + index, motion, entrance),
            )
        }
        if (state.tracks.isEmpty() && playlist.missingTrackCount == 0) {
            item(key = "empty", contentType = "note") {
                CollectionEmptyNote(
                    title = stringResource(R.string.playlists_empty),
                    hint = stringResource(R.string.playlists_empty_hint),
                    modifier = Modifier.animatedListItem(this, 2 + trackCount, motion, entrance),
                )
            }
        }
        if (playlist.missingTrackCount > 0) {
            item(key = "missing", contentType = "note") {
                UnavailableTracksNote(
                    count = playlist.missingTrackCount,
                    modifier = Modifier.animatedListItem(this, 2 + trackCount, motion, entrance),
                )
            }
        }
    }
}

@Composable
private fun rememberMedallionSettle(key: Long, motion: Boolean): () -> Float {
    var settled by rememberSaveable(key) { mutableStateOf(false) }
    val rotation = remember(key) { Animatable(if (settled || !motion) 0f else SETTLE_START_DEGREES) }
    LaunchedEffect(key, motion) {
        if (motion) {
            rotation.animateTo(0f, tween(SETTLE_MILLIS, easing = SettleEasing))
        } else {
            rotation.snapTo(0f)
        }
        settled = true
    }
    return remember(rotation) { { rotation.value } }
}

@Composable
private fun PlaylistDialogs(state: PlaylistDetailState, onIntent: (PlaylistDetailIntent) -> Unit) {
    val dismiss = { onIntent(PlaylistDetailIntent.DialogDismissed) }
    when (val dialog = state.dialog) {
        is PlaylistDetailDialog.Rename -> PlaylistNameSheet(
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
                tracks = previewPlaylist.tracks,
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
