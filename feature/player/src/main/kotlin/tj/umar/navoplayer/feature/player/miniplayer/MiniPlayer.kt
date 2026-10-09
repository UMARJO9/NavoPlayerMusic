package tj.umar.navoplayer.feature.player.miniplayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.rememberMedallionRotation
import tj.umar.navoplayer.core.designsystem.modifier.pressScale
import tj.umar.navoplayer.core.designsystem.theme.NavoShadows
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.feature.player.R
import tj.umar.navoplayer.core.ui.R as CoreUiR

private val MiniPlayerHeight = 68.dp
private val ProgressLineHeight = 3.dp
private const val TRACK_CHANGE_MILLIS = 300
private const val MINI_PLAYER_PRESSED_SCALE = 0.98f

@Composable
internal fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: () -> Float,
    onIntent: (MiniPlayerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    val rotation = rememberMedallionRotation(running = isPlaying)
    val openInteraction = remember { MutableInteractionSource() }
    val openLabel = stringResource(R.string.player_open_now_playing)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .pressScale(openInteraction, pressedScale = MINI_PLAYER_PRESSED_SCALE)
            .dropShadow(NavoShapes.MiniPlayer, NavoShadows.MiniPlayer)
            .clip(NavoShapes.MiniPlayer)
            .background(colors.high)
            .drawWithContent {
                drawContent()
                drawRect(
                    color = colors.accent,
                    topLeft = Offset.Zero,
                    size = Size(size.width * progress().coerceIn(0f, 1f), ProgressLineHeight.toPx()),
                )
            }
            .padding(end = 10.dp),
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
                .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = track,
                contentKey = { it.id },
                contentAlignment = Alignment.CenterStart,
                transitionSpec = { miniTrackTransition() },
                label = "miniTrackChange",
            ) { shownTrack ->
                MiniTrackInfo(track = shownTrack, rotation = rotation)
            }
        }
        NavoIconButton(
            icon = if (isPlaying) NavoIcons.Pause else NavoIcons.Play,
            contentDescription = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
            onClick = { onIntent(MiniPlayerIntent.PlayPauseClicked) },
            size = 48.dp,
            iconSize = 26.dp,
            animateIconChange = true,
        )
    }
}

@Composable
private fun MiniTrackInfo(track: Track, rotation: () -> Float) {
    val colors = NavoTheme.colors
    val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Medallion(palette = palette, modifier = Modifier.size(46.dp), rotationDegrees = rotation)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
}

private fun miniTrackTransition(): ContentTransform = ContentTransform(
    targetContentEnter = slideInVertically(tween(TRACK_CHANGE_MILLIS)) { height -> height / 2 } +
        fadeIn(tween(TRACK_CHANGE_MILLIS)),
    initialContentExit = slideOutVertically(tween(TRACK_CHANGE_MILLIS)) { height -> -height / 2 } +
        fadeOut(tween(TRACK_CHANGE_MILLIS)),
    sizeTransform = SizeTransform(clip = false),
)

@Preview(widthDp = 390, heightDp = 120)
@Composable
private fun MiniPlayerPreview() {
    NavoTheme {
        Column(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(12.dp),
        ) {
            MiniPlayer(
                track = Track(2, "Ҷавонӣ", "Daler Nazarov", null, 11, 101, 252_000, 2, "content://media/2", "Music/Navo", null, null),
                isPlaying = true,
                progress = { 0.38f },
                onIntent = {},
            )
        }
    }
}
