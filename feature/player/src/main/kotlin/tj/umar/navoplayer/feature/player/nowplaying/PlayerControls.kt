package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.modifier.pressScale
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.feature.player.R

private const val TOGGLE_COLOR_MILLIS = 200

@Composable
internal fun PlayerControls(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleControl(
            icon = NavoIcons.Shuffle,
            description = stringResource(R.string.player_shuffle),
            active = shuffleEnabled,
            onClick = { onIntent(NowPlayingIntent.ShuffleClicked) },
        )
        NavoIconButton(
            icon = NavoIcons.SkipPrevious,
            contentDescription = stringResource(R.string.player_previous),
            onClick = { onIntent(NowPlayingIntent.PreviousClicked) },
            size = 56.dp,
            iconSize = 32.dp,
        )
        PlayPauseButton(isPlaying = isPlaying, onClick = { onIntent(NowPlayingIntent.PlayPauseClicked) })
        NavoIconButton(
            icon = NavoIcons.SkipNext,
            contentDescription = stringResource(R.string.player_next),
            onClick = { onIntent(NowPlayingIntent.NextClicked) },
            size = 56.dp,
            iconSize = 32.dp,
        )
        ToggleControl(
            icon = NavoIcons.Repeat,
            description = stringResource(
                when (repeatMode) {
                    RepeatMode.Off -> R.string.player_repeat_off
                    RepeatMode.All -> R.string.player_repeat_all
                    RepeatMode.One -> R.string.player_repeat_one
                },
            ),
            active = repeatMode != RepeatMode.Off,
            onClick = { onIntent(NowPlayingIntent.RepeatClicked) },
            badge = if (repeatMode == RepeatMode.One) stringResource(R.string.player_repeat_one_badge) else null,
        )
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val description = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play)
    Box(
        modifier = Modifier
            .pressScale(interactionSource, pressedScale = 0.92f)
            .size(80.dp)
            .clip(CircleShape)
            .background(colors.accent)
            .semantics { contentDescription = description }
            .toggleable(
                value = isPlaying,
                interactionSource = interactionSource,
                indication = ripple(color = colors.onAccent),
                role = Role.Button,
                onValueChange = { onClick() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { (fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.8f)) togetherWith fadeOut(tween(100)) },
            label = "playPause",
        ) { playing ->
            Icon(
                imageVector = if (playing) NavoIcons.Pause else NavoIcons.Play,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun ToggleControl(
    icon: ImageVector,
    description: String,
    active: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val tint by animateColorAsState(
        targetValue = if (active) colors.accent else colors.contentSecondary,
        animationSpec = tween(TOGGLE_COLOR_MILLIS),
        label = "toggleTint",
    )
    Box(
        modifier = Modifier
            .pressScale(interactionSource, pressedScale = 0.9f)
            .size(48.dp)
            .semantics { contentDescription = description }
            .toggleable(
                value = active,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp, color = colors.content),
                role = Role.Switch,
                onValueChange = { onClick() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(4.dp)
                    .background(colors.accent, CircleShape),
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .sizeIn(minWidth = 14.dp, minHeight = 14.dp)
                    .background(colors.accent, CircleShape)
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    color = colors.onAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
