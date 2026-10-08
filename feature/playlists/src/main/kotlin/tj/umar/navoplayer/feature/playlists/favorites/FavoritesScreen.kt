package tj.umar.navoplayer.feature.playlists.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import tj.umar.navoplayer.feature.playlists.component.CollectionEmptyNote
import tj.umar.navoplayer.feature.playlists.component.CollectionHeader
import tj.umar.navoplayer.feature.playlists.component.CollectionSummary
import tj.umar.navoplayer.feature.playlists.component.UnavailableTracksNote
import tj.umar.navoplayer.feature.playlists.detail.previewPlaylist

@Composable
internal fun FavoritesScreen(
    state: FavoritesState,
    onIntent: (FavoritesIntent) -> Unit,
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
                contentDescription = stringResource(R.string.playlists_back),
                onClick = { onIntent(FavoritesIntent.BackClicked) },
            )
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
            if (favorites != null) FavoriteTrackList(state = state, favorites = favorites, onIntent = onIntent)
        }
    }
}

@Composable
private fun FavoriteTrackList(state: FavoritesState, favorites: FavoriteTracks, onIntent: (FavoritesIntent) -> Unit) {
    val removeLabel = stringResource(R.string.playlists_favorites_remove_label)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = navoListPadding(state.hasActivePlayback),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "header", contentType = "header") {
            CollectionHeader(title = stringResource(CoreUiR.string.core_ui_favorites)) {
                GroupArtwork(leading = GroupLeading.Favorites, size = 104.dp)
            }
        }
        item(key = "summary", contentType = "summary") {
            CollectionSummary(
                trackCount = favorites.tracks.size,
                totalMinutes = state.totalMinutes,
                isPlaying = state.isFavoritesPlaying,
                onPlay = { onIntent(FavoritesIntent.PlayClicked) },
                onShuffle = { onIntent(FavoritesIntent.ShuffleClicked) },
            )
        }
        items(favorites.tracks, key = { it.id }, contentType = { "track" }) { track ->
            TrackListItem(
                track = track,
                isCurrent = track.id == state.currentTrackId,
                isPlaying = track.id == state.currentTrackId && state.isPlaying,
                onClick = { onIntent(FavoritesIntent.TrackClicked(track.id)) },
                onLongClick = { onIntent(FavoritesIntent.TrackLongPressed(track.id)) },
                onLongClickLabel = removeLabel,
                modifier = Modifier.animateItem(),
            )
        }
        if (favorites.tracks.isEmpty() && favorites.missingTrackCount == 0) {
            item(key = "empty", contentType = "note") {
                CollectionEmptyNote(
                    title = stringResource(R.string.playlists_favorites_empty),
                    hint = stringResource(R.string.playlists_favorites_empty_hint),
                )
            }
        }
        if (favorites.missingTrackCount > 0) {
            item(key = "missing", contentType = "note") {
                UnavailableTracksNote(count = favorites.missingTrackCount)
            }
        }
    }
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
