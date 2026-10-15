package tj.umar.navoplayer.feature.player.miniplayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.ProgressRing
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionArtwork
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.khotamWatermark
import tj.umar.navoplayer.core.designsystem.modifier.pressScale
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.artworkKey
import tj.umar.navoplayer.core.ui.artwork.rememberArtwork
import tj.umar.navoplayer.feature.player.R
import tj.umar.navoplayer.core.ui.R as CoreUiR

private val MiniPlayerHeight = 68.dp
private val RingSlot = 58.dp
private val MedallionSize = 42.dp
private val ControlSize = 48.dp
private val ControlGap = 2.dp
private val NextIconSize = 22.dp
private val PlayIconSize = 24.dp
private val HairlineWidth = 1.dp
private val CardEndPadding = 8.dp
private val OpenAreaStartPadding = 5.dp
private val OpenAreaEndPadding = 8.dp
private const val TRACK_CHANGE_MILLIS = 300
private const val MEDALLION_CHANGE_SCALE = 0.85f
private const val MINI_PLAYER_PRESSED_SCALE = 0.98f
private const val PALETTE_TINT_FRACTION = 0.16f
private const val PALETTE_TINT_MILLIS = 450
private const val TINT_STOP = 0.45f
private const val END_SHADE_FRACTION = 0.35f
private const val HAIRLINE_TOP_ALPHA = 0.16f
private const val HAIRLINE_BOTTOM_ALPHA = 0.04f
private const val WATERMARK_ALPHA = 0.06f

@Composable
internal fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    hasNext: Boolean,
    progress: () -> Float,
    rotation: () -> Float,
    onIntent: (MiniPlayerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    val openInteraction = remember { MutableInteractionSource() }
    val openLabel = stringResource(R.string.player_open_now_playing)
    val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
    val tint = animateColorAsState(
        targetValue = lerp(colors.high, palette.background, PALETTE_TINT_FRACTION),
        animationSpec = tween(PALETTE_TINT_MILLIS),
        label = "miniPlayerTint",
    )
    val endColor = remember(colors) { lerp(colors.high, colors.raised, END_SHADE_FRACTION) }
    val hairline = remember(colors) {
        Brush.verticalGradient(
            listOf(colors.highlight.copy(alpha = HAIRLINE_TOP_ALPHA), colors.highlight.copy(alpha = HAIRLINE_BOTTOM_ALPHA)),
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .pressScale(openInteraction, pressedScale = MINI_PLAYER_PRESSED_SCALE)
            .dropShadow(NavoShapes.MiniPlayer, NavoTheme.shadows.miniPlayer)
            .clip(NavoShapes.MiniPlayer)
            .drawBehind {
                drawRect(
                    Brush.horizontalGradient(
                        0f to tint.value,
                        TINT_STOP to colors.high,
                        1f to endColor,
                    ),
                )
            }
            .khotamWatermark(colors.content.copy(alpha = WATERMARK_ALPHA))
            .border(HairlineWidth, hairline, NavoShapes.MiniPlayer)
            .padding(end = CardEndPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    interactionSource = openInteraction,
                    indication = ripple(color = colors.content),
                    onClickLabel = openLabel,
                    role = Role.Button,
                ) { onIntent(MiniPlayerIntent.OpenClicked) }
                .padding(start = OpenAreaStartPadding, end = OpenAreaEndPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgressRing(progress = progress, modifier = Modifier.size(RingSlot), glowing = isPlaying) {
                AnimatedContent(
                    targetState = track,
                    contentKey = { it.id },
                    transitionSpec = { miniMedallionTransition() },
                    label = "miniMedallionChange",
                ) { shownTrack ->
                    val palette = remember(shownTrack.id) { MedallionPalettes.forKey(shownTrack.id) }
                    MedallionArtwork(
                        palette = palette,
                        image = rememberArtwork(shownTrack.artworkKey, MedallionSize).value,
                        modifier = Modifier.size(MedallionSize),
                        rotationDegrees = rotation,
                    )
                }
            }
            AnimatedContent(
                targetState = track,
                contentKey = { it.id },
                contentAlignment = Alignment.CenterStart,
                transitionSpec = { miniTrackTransition() },
                label = "miniTrackChange",
            ) { shownTrack ->
                MiniTrackInfo(track = shownTrack)
            }
        }
        NavoIconButton(
            icon = NavoIcons.SkipNext,
            contentDescription = stringResource(R.string.player_next),
            onClick = { onIntent(MiniPlayerIntent.NextClicked) },
            size = ControlSize,
            iconSize = NextIconSize,
            enabled = hasNext,
        )
        Spacer(Modifier.width(ControlGap))
        NavoIconButton(
            icon = if (isPlaying) NavoIcons.Pause else NavoIcons.Play,
            contentDescription = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
            onClick = { onIntent(MiniPlayerIntent.PlayPauseClicked) },
            containerColor = colors.accent,
            tint = colors.onAccent,
            size = ControlSize,
            iconSize = PlayIconSize,
            animateIconChange = true,
        )
    }
}

@Composable
private fun MiniTrackInfo(track: Track) {
    val colors = NavoTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = track.title.ifBlank { stringResource(CoreUiR.string.core_ui_unknown_title) },
            color = colors.content,
            style = NavoTheme.typography.label.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = track.artist ?: stringResource(CoreUiR.string.core_ui_unknown_artist),
            color = colors.contentOnHigh,
            style = NavoTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun miniMedallionTransition(): ContentTransform = ContentTransform(
    targetContentEnter = fadeIn(tween(TRACK_CHANGE_MILLIS)) +
        scaleIn(tween(TRACK_CHANGE_MILLIS), initialScale = MEDALLION_CHANGE_SCALE),
    initialContentExit = fadeOut(tween(TRACK_CHANGE_MILLIS)) +
        scaleOut(tween(TRACK_CHANGE_MILLIS), targetScale = MEDALLION_CHANGE_SCALE),
    sizeTransform = SizeTransform(clip = false),
)

private fun miniTrackTransition(): ContentTransform = ContentTransform(
    targetContentEnter = slideInVertically(tween(TRACK_CHANGE_MILLIS)) { height -> height / 2 } +
        fadeIn(tween(TRACK_CHANGE_MILLIS)),
    initialContentExit = slideOutVertically(tween(TRACK_CHANGE_MILLIS)) { height -> -height / 2 } +
        fadeOut(tween(TRACK_CHANGE_MILLIS)),
    sizeTransform = SizeTransform(clip = false),
)

private fun previewTrack(id: Long, title: String, artist: String?) =
    Track(id, title, artist, null, 11, 101, 252_000, 2, "content://media/$id", "Music/Navo", null, null)

@Composable
private fun MiniPlayerPreviewFrame(content: @Composable () -> Unit) {
    NavoTheme {
        Column(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(12.dp),
        ) {
            content()
        }
    }
}

@Preview(widthDp = 390, heightDp = 120)
@Composable
private fun MiniPlayerPlayingPreview() {
    MiniPlayerPreviewFrame {
        MiniPlayer(
            track = previewTrack(2, "Ҷавонӣ", "Daler Nazarov"),
            isPlaying = true,
            hasNext = true,
            progress = { 0.38f },
            rotation = { 0f },
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 120)
@Composable
private fun MiniPlayerPausedPreview() {
    MiniPlayerPreviewFrame {
        MiniPlayer(
            track = previewTrack(5, "Over the Horizon", "Samsung"),
            isPlaying = false,
            hasNext = false,
            progress = { 0.72f },
            rotation = { 0f },
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 120, fontScale = 1.3f)
@Composable
private fun MiniPlayerLongTextPreview() {
    MiniPlayerPreviewFrame {
        MiniPlayer(
            track = previewTrack(4, "AudioCleaner_20260908_212257_VID_20260908_212002_189", null),
            isPlaying = true,
            hasNext = true,
            progress = { 0.05f },
            rotation = { 0f },
            onIntent = {},
        )
    }
}
