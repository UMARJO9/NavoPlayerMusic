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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.ProgressRing
import tj.umar.navoplayer.core.designsystem.glass.LocalNavoGlassBackdrop
import tj.umar.navoplayer.core.designsystem.glass.NavoGlassDefaults
import tj.umar.navoplayer.core.designsystem.glass.navoGlass
import tj.umar.navoplayer.core.designsystem.glass.navoGlassSource
import tj.umar.navoplayer.core.designsystem.glass.rememberNavoGlassBackdrop
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
private val CardEndPadding = 8.dp
private val OpenAreaStartPadding = 5.dp
private val OpenAreaEndPadding = 8.dp
private const val TRACK_CHANGE_MILLIS = 300
private const val MEDALLION_CHANGE_SCALE = 0.85f
private const val MINI_PLAYER_PRESSED_SCALE = 0.98f
private const val PALETTE_OVERLAY_ALPHA = 0.22f
private const val LIGHT_PALETTE_OVERLAY_ALPHA = 0.12f
private const val PALETTE_TINT_MILLIS = 450
private const val TINT_STOP = 0.45f
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
    val glassStyle = NavoGlassDefaults.bar()
    val openInteraction = remember { MutableInteractionSource() }
    val openLabel = stringResource(R.string.player_open_now_playing)
    val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
    val overlayAlpha = if (colors.isDark) PALETTE_OVERLAY_ALPHA else LIGHT_PALETTE_OVERLAY_ALPHA
    val tint = animateColorAsState(
        targetValue = palette.background.copy(alpha = overlayAlpha),
        animationSpec = tween(PALETTE_TINT_MILLIS),
        label = "miniPlayerTint",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .pressScale(openInteraction, pressedScale = MINI_PLAYER_PRESSED_SCALE)
            .dropShadow(NavoShapes.GlassMiniPlayer, NavoTheme.shadows.miniPlayer)
            .navoGlass(LocalNavoGlassBackdrop.current, NavoShapes.GlassMiniPlayer, glassStyle)
            .clip(NavoShapes.GlassMiniPlayer)
            .drawBehind {
                drawRect(
                    Brush.horizontalGradient(
                        0f to tint.value,
                        TINT_STOP to tint.value.copy(alpha = 0f),
                    ),
                )
            }
            .khotamWatermark(colors.content.copy(alpha = WATERMARK_ALPHA))
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
            ProgressRing(
                progress = progress,
                modifier = Modifier
                    .size(RingSlot)
                    .graphicsLayer(),
                glowing = isPlaying,
            ) {
                AnimatedContent(
                    targetState = track,
                    modifier = Modifier.graphicsLayer(),
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
            color = NavoGlassDefaults.bar().secondaryContent,
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

private val PreviewStripes = listOf(
    Color(0xFFE3B04B),
    Color(0xFFC8414B),
    Color(0xFF2E7D74),
    Color(0xFF24387A),
    Color(0xFFF3EEDF),
)

@Composable
private fun MiniPlayerPreviewFrame(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    NavoTheme(darkTheme = darkTheme) {
        val backdrop = rememberNavoGlassBackdrop()
        Box(modifier = Modifier.background(NavoTheme.colors.background)) {
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .navoGlassSource(backdrop)
                    .background(NavoTheme.colors.background),
            ) {
                PreviewStripes.forEach { stripe ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(stripe),
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                CompositionLocalProvider(LocalNavoGlassBackdrop provides backdrop) {
                    content()
                }
            }
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
private fun MiniPlayerLightPreview() {
    MiniPlayerPreviewFrame(darkTheme = false) {
        MiniPlayer(
            track = previewTrack(3, "Шаби ёд", "Shabnami Surayo"),
            isPlaying = true,
            hasNext = true,
            progress = { 0.54f },
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
