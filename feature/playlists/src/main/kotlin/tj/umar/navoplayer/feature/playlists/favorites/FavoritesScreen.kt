package tj.umar.navoplayer.feature.playlists.favorites

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.animatedListItem
import tj.umar.navoplayer.core.designsystem.animation.rememberListEntrance
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.component.GroupArtwork
import tj.umar.navoplayer.core.designsystem.component.GroupLeading
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.component.contentPhase
import tj.umar.navoplayer.core.designsystem.component.navoListPadding
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.FavoriteTracks
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.core.ui.track.TrackListItem
import tj.umar.navoplayer.feature.playlists.R
import tj.umar.navoplayer.feature.playlists.component.CollapsedTitle
import tj.umar.navoplayer.feature.playlists.component.CollectionEmptyNote
import tj.umar.navoplayer.feature.playlists.component.CollectionHeader
import tj.umar.navoplayer.feature.playlists.component.CollectionSummary
import tj.umar.navoplayer.feature.playlists.component.HEADER_ITEM_KEY
import tj.umar.navoplayer.feature.playlists.component.UnavailableTracksNote
import tj.umar.navoplayer.feature.playlists.component.headerCollapseFraction
import tj.umar.navoplayer.feature.playlists.detail.previewPlaylist

private const val HEART_BEAT_START_SCALE = 0.9f
private const val HEART_BEAT_DAMPING = 0.45f

@Composable
internal fun FavoritesScreen(
    state: FavoritesState,
    onIntent: (FavoritesIntent) -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavoIconButton(
                icon = NavoIcons.ChevronLeft,
                contentDescription = stringResource(R.string.playlists_back),
                onClick = { onIntent(FavoritesIntent.BackClicked) },
            )
            CollapsedTitle(
                title = stringResource(CoreUiR.string.core_ui_favorites),
                collapse = collapse,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            Spacer(modifier = Modifier.size(44.dp))
        }
        PhasedContent(
            phase = contentPhase(
                hasContent = state.favorites != null,
                isLoading = state.isLoading,
                loadFailed = state.loadFailed,
            ),
            modifier = Modifier.fillMaxSize(),
            error = {
                StateMessage(
                    message = stringResource(R.string.playlists_favorites_error),
                    palette = MedallionPalettes.all[0],
                    actionLabel = stringResource(R.string.playlists_retry),
                    onAction = { onIntent(FavoritesIntent.RetryLoad) },
                )
            },
        ) {
            val favorites = state.favorites
            if (favorites != null) {
                FavoriteTrackList(
                    state = state,
                    favorites = favorites,
                    listState = listState,
                    collapse = collapse,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun FavoriteTrackList(
    state: FavoritesState,
    favorites: FavoriteTracks,
    listState: LazyListState,
    collapse: () -> Float,
    onIntent: (FavoritesIntent) -> Unit,
) {
    val removeLabel = stringResource(R.string.playlists_favorites_remove_label)
    val motion = rememberMotionEnabled()
    val entrance = rememberListEntrance()
    val heartBeat = rememberHeartBeat(motion)
    val trackCount = favorites.tracks.size
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = navoListPadding(state.hasActivePlayback),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = HEADER_ITEM_KEY, contentType = "header") {
            CollectionHeader(
                title = stringResource(CoreUiR.string.core_ui_favorites),
                modifier = Modifier.animatedListItem(this, 0, motion, entrance),
                collapse = collapse,
            ) {
                GroupArtwork(
                    leading = GroupLeading.Favorites,
                    size = 104.dp,
                    modifier = Modifier.graphicsLayer {
                        val scale = heartBeat()
                        scaleX = scale
                        scaleY = scale
                    },
                )
            }
        }
        item(key = "summary", contentType = "summary") {
            CollectionSummary(
                trackCount = trackCount,
                totalMinutes = state.totalMinutes,
                isPlaying = state.isFavoritesPlaying,
                onPlay = { onIntent(FavoritesIntent.PlayClicked) },
                onShuffle = { onIntent(FavoritesIntent.ShuffleClicked) },
                modifier = Modifier.animatedListItem(this, 1, motion, entrance),
            )
        }
        itemsIndexed(favorites.tracks, key = { _, track -> track.id }, contentType = { _, _ -> "track" }) { index, track ->
            TrackListItem(
                track = track,
                isCurrent = track.id == state.currentTrackId,
                isPlaying = track.id == state.currentTrackId && state.isPlaying,
                onClick = { onIntent(FavoritesIntent.TrackClicked(track.id)) },
                onLongClick = { onIntent(FavoritesIntent.TrackLongPressed(track.id)) },
                onLongClickLabel = removeLabel,
                modifier = Modifier.animatedListItem(this, 2 + index, motion, entrance),
            )
        }
        if (favorites.tracks.isEmpty() && favorites.missingTrackCount == 0) {
            item(key = "empty", contentType = "note") {
                CollectionEmptyNote(
                    title = stringResource(R.string.playlists_favorites_empty),
                    hint = stringResource(R.string.playlists_favorites_empty_hint),
                    modifier = Modifier.animatedListItem(this, 2 + trackCount, motion, entrance),
                )
            }
        }
        if (favorites.missingTrackCount > 0) {
            item(key = "missing", contentType = "note") {
                UnavailableTracksNote(
                    count = favorites.missingTrackCount,
                    modifier = Modifier.animatedListItem(this, 2 + trackCount, motion, entrance),
                )
            }
        }
    }
}

@Composable
private fun rememberHeartBeat(motion: Boolean): () -> Float {
    var played by rememberSaveable { mutableStateOf(false) }
    val scale = remember { Animatable(if (played || !motion) 1f else HEART_BEAT_START_SCALE) }
    LaunchedEffect(motion) {
        if (motion) {
            scale.animateTo(1f, spring(dampingRatio = HEART_BEAT_DAMPING, stiffness = Spring.StiffnessMediumLow))
        } else {
            scale.snapTo(1f)
        }
        played = true
    }
    return remember(scale) { { scale.value } }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun FavoritesPreview() {
    NavoTheme {
        FavoritesScreen(
            state = FavoritesState(
                isLoading = false,
                favorites = FavoriteTracks(tracks = previewPlaylist.tracks, missingTrackCount = 1),
                totalMinutes = 12,
                currentTrackId = previewPlaylist.tracks.first().id,
                currentSource = PlaybackSource.Favorites,
                isPlaying = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun EmptyFavoritesPreview() {
    NavoTheme {
        FavoritesScreen(
            state = FavoritesState(isLoading = false, favorites = FavoriteTracks(emptyList(), 0)),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 600)
@Composable
private fun FavoritesErrorPreview() {
    NavoTheme {
        FavoritesScreen(state = FavoritesState(isLoading = false, loadFailed = true), onIntent = {})
    }
}
