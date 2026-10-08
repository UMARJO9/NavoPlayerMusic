package tj.umar.navoplayer.feature.library.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.equalizer.EqualizerBars
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalette
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.ui.format.formatDuration
import tj.umar.navoplayer.feature.library.R

private const val PHASE_ENTER_MILLIS = 260
private const val PHASE_EXIT_MILLIS = 160
private val ListHorizontalPadding = 8.dp
private val SummaryHorizontalPadding = NavoSpacing.ScreenHorizontal - ListHorizontalPadding

private enum class TracksPhase { Loading, Content, Empty, Error }

private fun LibraryState.tracksPhase(): TracksPhase = when {
    tracks.isNotEmpty() -> TracksPhase.Content
    isLoadingTracks -> TracksPhase.Loading
    tracksLoadFailed -> TracksPhase.Error
    else -> TracksPhase.Empty
}

@Composable
internal fun TracksTabContent(
    state: LibraryState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = state.tracksPhase(),
        transitionSpec = { fadeIn(tween(PHASE_ENTER_MILLIS)) togetherWith fadeOut(tween(PHASE_EXIT_MILLIS)) },
        modifier = modifier,
        label = "tracksPhase",
    ) { phase ->
        when (phase) {
            TracksPhase.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavoTheme.colors.accent)
            }
            TracksPhase.Content -> TrackList(state = state, onIntent = onIntent)
            TracksPhase.Empty -> StateMessage(
                message = stringResource(R.string.library_tracks_empty),
                palette = MedallionPalettes.all[4],
            )
            TracksPhase.Error -> StateMessage(
                message = stringResource(R.string.library_tracks_error),
                palette = MedallionPalettes.all[0],
                actionLabel = stringResource(R.string.library_tracks_retry),
                onAction = { onIntent(LibraryIntent.RetryLoadTracks) },
            )
        }
    }
}

@Composable
private fun TrackList(state: LibraryState, onIntent: (LibraryIntent) -> Unit) {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ListHorizontalPadding,
            top = NavoSpacing.ExtraSmall,
            end = ListHorizontalPadding,
            bottom = NavoSpacing.ListBottomInset + navigationBarBottom,
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "summary", contentType = "summary") {
            TracksSummary(
                trackCount = state.tracks.size,
                totalMinutes = state.totalMinutes,
                onSortClick = { onIntent(LibraryIntent.SortClicked) },
                onShuffleClick = { onIntent(LibraryIntent.ShuffleClicked) },
            )
        }
        items(state.tracks, key = { it.id }, contentType = { "track" }) { track ->
            val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
            TrackRow(
                title = track.title.ifBlank { stringResource(R.string.library_unknown_title) },
                artist = track.artist ?: stringResource(R.string.library_unknown_artist),
                duration = formatDuration(track.durationMs),
                palette = palette,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun TracksSummary(
    trackCount: Int,
    totalMinutes: Int,
    onSortClick: () -> Unit,
    onShuffleClick: () -> Unit,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SummaryHorizontalPadding, top = 12.dp, end = SummaryHorizontalPadding, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pluralStringResource(R.plurals.library_track_count, trackCount, trackCount),
                style = typography.titleS,
                color = colors.content,
            )
            Text(
                text = pluralStringResource(R.plurals.library_minute_count, totalMinutes, totalMinutes),
                style = typography.secondary,
                color = colors.contentSecondary,
            )
        }
        NavoIconButton(
            icon = NavoIcons.Sort,
            contentDescription = stringResource(R.string.library_sort),
            onClick = onSortClick,
            containerColor = colors.raised,
            iconSize = 22.dp,
        )
        NavoButton(
            text = stringResource(R.string.library_shuffle),
            onClick = onShuffleClick,
            leadingIcon = NavoIcons.Shuffle,
            height = NavoSpacing.MinTouchTarget,
            contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
            textStyle = typography.label.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun TrackRow(
    title: String,
    artist: String,
    duration: String,
    palette: MedallionPalette,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(NavoShapes.TrackRow)
            .background(if (isCurrent) colors.raised else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Medallion(palette = palette, modifier = Modifier.size(48.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = typography.itemTitle,
                color = if (isCurrent) colors.accent else colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist,
                style = typography.secondary,
                color = colors.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.widthIn(min = 36.dp), contentAlignment = Alignment.CenterEnd) {
            if (isCurrent) {
                EqualizerBars(paused = !isPlaying)
            } else {
                Text(text = duration, style = typography.secondaryNumeric, color = colors.contentSecondary)
            }
        }
    }
}

@Composable
private fun StateMessage(
    message: String,
    palette: MedallionPalette,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = NavoSpacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Medallion(palette = palette, modifier = Modifier.size(104.dp))
            Text(
                text = message,
                style = NavoTheme.typography.titleS,
                color = NavoTheme.colors.content,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null) {
                NavoButton(text = actionLabel, onClick = onAction, height = NavoSpacing.MinTouchTarget)
            }
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

@Preview(widthDp = 390, heightDp = 200)
@Composable
private fun TrackRowCurrentPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.background).padding(8.dp)) {
            TrackRow(
                title = "Ҷавонӣ",
                artist = "Daler Nazarov",
                duration = "4:12",
                palette = MedallionPalettes.all[2],
                isCurrent = true,
                isPlaying = true,
            )
            TrackRow(
                title = "Утро в Варзобе",
                artist = "Navo Band",
                duration = "3:05",
                palette = MedallionPalettes.all[1],
            )
        }
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
