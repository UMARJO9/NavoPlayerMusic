package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberSmoothProgress
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.NavoSeekBar
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionArtwork
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.MedallionVariant
import tj.umar.navoplayer.core.designsystem.medallion.rememberMedallionRotation
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.artworkKey
import tj.umar.navoplayer.core.ui.artwork.rememberArtwork
import tj.umar.navoplayer.core.ui.format.formatDuration
import tj.umar.navoplayer.feature.player.R
import tj.umar.navoplayer.core.ui.R as CoreUiR

private val MedallionMaxSize = 300.dp
private const val TRACK_CHANGE_MILLIS = 350
private const val MEDALLION_CHANGE_SCALE = 0.85f
private const val TRACK_INFO_SLIDE_DIVISOR = 3

@Composable
internal fun NowPlayingScreen(
    state: NowPlayingState,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = NavoSpacing.ScreenHorizontal, top = 4.dp, end = NavoSpacing.ScreenHorizontal, bottom = 20.dp),
    ) {
        TopBar(source = state.source, onIntent = onIntent)
        val track = state.track
        val medallionRotation = rememberMedallionRotation(running = state.isPlaying)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (track != null) {
                AnimatedContent(
                    targetState = track,
                    contentKey = { it.id },
                    contentAlignment = Alignment.Center,
                    transitionSpec = { medallionTransition(state.trackChangeDirection) },
                    label = "medallionChange",
                ) { shownTrack ->
                    RotatingMedallion(track = shownTrack, rotationDegrees = medallionRotation)
                }
            }
        }
        if (track != null) {
            AnimatedContent(
                targetState = track,
                contentKey = { it.id },
                transitionSpec = { trackInfoTransition(state.trackChangeDirection) },
                label = "trackInfoChange",
            ) { shownTrack ->
                val isCurrent = shownTrack.id == state.track?.id
                val shownFavorite = remember { mutableStateOf(state.isFavorite) }
                if (isCurrent) shownFavorite.value = state.isFavorite
                TrackInfo(
                    track = shownTrack,
                    isFavorite = shownFavorite.value,
                    onIntent = { intent -> if (isCurrent) onIntent(intent) },
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        SeekBar(state = state, onIntent = onIntent)
        Spacer(modifier = Modifier.height(20.dp))
        PlayerControls(
            isPlaying = state.isPlaying,
            shuffleEnabled = state.shuffleEnabled,
            repeatMode = state.repeatMode,
            onIntent = onIntent,
        )
        Spacer(modifier = Modifier.height(28.dp))
        BottomRow(nextTrack = state.nextTrack, sleepTimer = state.sleepTimer, onIntent = onIntent)
    }
}

private fun medallionTransition(direction: TrackChangeDirection): ContentTransform {
    val sign = direction.sign()
    val enter = slideInHorizontally(tween(TRACK_CHANGE_MILLIS)) { width -> sign * width / 2 } +
        fadeIn(tween(TRACK_CHANGE_MILLIS)) +
        scaleIn(tween(TRACK_CHANGE_MILLIS), initialScale = MEDALLION_CHANGE_SCALE)
    val exit = slideOutHorizontally(tween(TRACK_CHANGE_MILLIS)) { width -> -sign * width / 2 } +
        fadeOut(tween(TRACK_CHANGE_MILLIS)) +
        scaleOut(tween(TRACK_CHANGE_MILLIS), targetScale = MEDALLION_CHANGE_SCALE)
    return ContentTransform(
        targetContentEnter = enter,
        initialContentExit = exit,
        sizeTransform = SizeTransform(clip = false) { _, _ -> tween(TRACK_CHANGE_MILLIS) },
    )
}

private fun trackInfoTransition(direction: TrackChangeDirection): ContentTransform {
    val sign = direction.sign()
    val enter = slideInHorizontally(tween(TRACK_CHANGE_MILLIS)) { width -> sign * width / TRACK_INFO_SLIDE_DIVISOR } +
        fadeIn(tween(TRACK_CHANGE_MILLIS))
    val exit = slideOutHorizontally(tween(TRACK_CHANGE_MILLIS)) { width -> -sign * width / TRACK_INFO_SLIDE_DIVISOR } +
        fadeOut(tween(TRACK_CHANGE_MILLIS))
    return ContentTransform(
        targetContentEnter = enter,
        initialContentExit = exit,
        sizeTransform = SizeTransform(clip = false) { _, _ -> tween(TRACK_CHANGE_MILLIS) },
    )
}

private fun TrackChangeDirection.sign(): Int = when (this) {
    TrackChangeDirection.Forward -> 1
    TrackChangeDirection.Backward -> -1
}

@Composable
private fun TopBar(source: PlaybackSource?, onIntent: (NowPlayingIntent) -> Unit) {
    val colors = NavoTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        NavoIconButton(
            icon = NavoIcons.ChevronDown,
            contentDescription = stringResource(R.string.player_collapse),
            onClick = { onIntent(NowPlayingIntent.CollapseClicked) },
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (source != null) {
                Text(
                    text = stringResource(R.string.player_playing_from),
                    style = NavoTheme.typography.caption,
                    color = colors.contentSecondary,
                )
                Text(
                    text = source.label(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = NavoTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.content,
                )
            }
        }
        NavoIconButton(
            icon = NavoIcons.More,
            contentDescription = stringResource(R.string.player_more),
            onClick = { onIntent(NowPlayingIntent.MoreClicked) },
        )
    }
}

@Composable
private fun RotatingMedallion(track: Track, rotationDegrees: () -> Float) {
    val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
    Box(
        modifier = Modifier
            .sizeIn(maxWidth = MedallionMaxSize, maxHeight = MedallionMaxSize)
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .dropShadow(CircleShape, NavoTheme.shadows.medallion),
    ) {
        MedallionArtwork(
            palette = palette,
            image = rememberArtwork(track.artworkKey, MedallionMaxSize).value,
            modifier = Modifier.fillMaxSize(),
            variant = MedallionVariant.Detailed,
            rotationDegrees = rotationDegrees,
        )
    }
}

@Composable
private fun TrackInfo(track: Track, isFavorite: Boolean, onIntent: (NowPlayingIntent) -> Unit) {
    val colors = NavoTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = track.title.ifBlank { stringResource(CoreUiR.string.core_ui_unknown_title) },
                style = NavoTheme.typography.displayM,
                color = colors.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist ?: stringResource(CoreUiR.string.core_ui_unknown_artist),
                style = NavoTheme.typography.body.copy(lineHeight = NavoTheme.typography.itemTitle.lineHeight),
                color = colors.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        NavoIconButton(
            icon = if (isFavorite) NavoIcons.HeartFilled else NavoIcons.Heart,
            contentDescription = stringResource(
                if (isFavorite) R.string.player_favorite_remove else R.string.player_favorite_add,
            ),
            onClick = { onIntent(NowPlayingIntent.FavoriteClicked) },
            size = 48.dp,
            iconSize = 26.dp,
            modifier = Modifier.semantics { selected = isFavorite },
            tint = if (isFavorite) colors.accent else colors.content,
            animateIconChange = true,
        )
    }
}

@Composable
private fun SeekBar(state: NowPlayingState, onIntent: (NowPlayingIntent) -> Unit) {
    val colors = NavoTheme.colors
    val duration = state.durationMs.coerceAtLeast(0)
    val position = state.displayedPositionMs.coerceIn(0, duration.coerceAtLeast(0))
    val positionText = formatDuration(position)
    val durationText = formatDuration(duration)
    val smoothProgress = rememberSmoothProgress(
        positionMs = { state.displayedPositionMs },
        durationMs = { state.durationMs },
        playing = state.isPlaying && state.seekPreviewMs == null,
        snap = state.seekPreviewMs != null,
    )
    val previewMs = state.seekPreviewMs
    val value: () -> Float = if (previewMs != null && duration > 0) {
        { previewMs.coerceIn(0, duration).toFloat() / duration }
    } else {
        smoothProgress
    }
    Column {
        SmoothSeekBar(
            value = value,
            onValueChange = { fraction -> onIntent(NowPlayingIntent.SeekChanged((fraction * duration).toLong())) },
            onValueChangeFinished = { onIntent(NowPlayingIntent.SeekFinished) },
            playing = state.isPlaying,
            stateDescription = stringResource(R.string.player_seek_state, positionText, durationText),
            contentDescription = stringResource(R.string.player_seek),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = positionText, style = NavoTheme.typography.captionNumeric, color = colors.contentSecondary)
            Text(text = durationText, style = NavoTheme.typography.captionNumeric, color = colors.contentSecondary)
        }
    }
}

@Composable
private fun SmoothSeekBar(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    playing: Boolean,
    stateDescription: String,
    contentDescription: String,
) {
    NavoSeekBar(
        value = value(),
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        playing = playing,
        stateDescription = stateDescription,
        contentDescription = contentDescription,
    )
}

@Composable
private fun BottomRow(nextTrack: Track?, sleepTimer: SleepTimer, onIntent: (NowPlayingIntent) -> Unit) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    val queueLabel = stringResource(R.string.player_open_queue)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(CircleShape)
                .background(colors.raised)
                .clickable(onClickLabel = queueLabel, role = Role.Button) { onIntent(NowPlayingIntent.QueueClicked) }
                .padding(start = 14.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = NavoIcons.Queue, contentDescription = null, tint = colors.content, modifier = Modifier.size(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(text = stringResource(R.string.player_up_next), style = typography.caption, color = colors.contentSecondary)
                Text(
                    text = nextTrack?.let {
                        stringResource(
                            R.string.player_up_next_track,
                            it.title.ifBlank { stringResource(CoreUiR.string.core_ui_unknown_title) },
                            it.artist ?: stringResource(CoreUiR.string.core_ui_unknown_artist),
                        )
                    } ?: stringResource(R.string.player_up_next_none),
                    style = typography.label,
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        SleepTimerButton(sleepTimer = sleepTimer, onClick = { onIntent(NowPlayingIntent.SleepTimerClicked) })
    }
}

@Composable
private fun PlaybackSource.label(): String = when (this) {
    PlaybackSource.AllTracks -> stringResource(R.string.player_source_all_tracks)
    is PlaybackSource.Album -> title ?: stringResource(CoreUiR.string.core_ui_unknown_album)
    is PlaybackSource.Artist -> name ?: stringResource(CoreUiR.string.core_ui_unknown_artist)
    is PlaybackSource.Folder -> name ?: stringResource(CoreUiR.string.core_ui_unknown_folder)
    is PlaybackSource.Search -> stringResource(R.string.player_source_search, query)
    is PlaybackSource.Playlist -> name
    PlaybackSource.Favorites -> stringResource(CoreUiR.string.core_ui_favorites)
    PlaybackSource.Queue -> stringResource(R.string.player_source_queue)
}

@Composable
private fun SheetPreviewFrame(content: @Composable () -> Unit) {
    NavoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavoTheme.colors.background, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        ) {
            content()
        }
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun NowPlayingScreenPreview() {
    SheetPreviewFrame {
        NowPlayingScreen(state = previewNowPlayingState, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun NowPlayingScreenRepeatOnePreview() {
    SheetPreviewFrame {
        NowPlayingScreen(
            state = previewNowPlayingState.copy(isPlaying = false, shuffleEnabled = true, isFavorite = true, repeatMode = RepeatMode.One),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun NowPlayingScreenSleepTimerPreview() {
    SheetPreviewFrame {
        NowPlayingScreen(state = previewNowPlayingState.copy(sleepTimer = SleepTimer.Countdown(872_000, 900_000)), onIntent = {})
    }
}
