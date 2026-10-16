package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.glass.LocalNavoGlassBackdrop
import tj.umar.navoplayer.core.designsystem.glass.NavoGlassDefaults
import tj.umar.navoplayer.core.designsystem.glass.navoGlass
import tj.umar.navoplayer.core.designsystem.glass.navoGlassSource
import tj.umar.navoplayer.core.designsystem.glass.rememberNavoGlassBackdrop
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.modifier.pressScale
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.ui.format.formatDuration
import tj.umar.navoplayer.feature.player.R

private val ButtonHeight = 56.dp
private val TimerIconSize = 22.dp
private const val PRESSED_SCALE = 0.9f

@Composable
internal fun SleepTimerButton(sleepTimer: SleepTimer, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NavoTheme.colors
    val backdrop = LocalNavoGlassBackdrop.current
    val glassStyle = NavoGlassDefaults.pill()
    if (sleepTimer == SleepTimer.Off) {
        val interactionSource = remember { MutableInteractionSource() }
        val description = stringResource(R.string.player_sleep_timer)
        Box(
            modifier = modifier
                .pressScale(interactionSource, pressedScale = PRESSED_SCALE)
                .size(ButtonHeight)
                .navoGlass(backdrop, NavoShapes.GlassPill, glassStyle)
                .clip(NavoShapes.GlassPill)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = colors.content),
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = NavoIcons.Timer, contentDescription = null, tint = colors.content, modifier = Modifier.size(24.dp))
        }
        return
    }
    val label = sleepTimer.accessibilityLabel()
    val changeLabel = stringResource(R.string.player_sleep_timer_change)
    val text = when (sleepTimer) {
        is SleepTimer.Countdown -> formatDuration(sleepTimer.remainingMs.ceilToSecond())
        else -> stringResource(R.string.player_sleep_timer_end_of_track_short)
    }
    Row(
        modifier = modifier
            .height(ButtonHeight)
            .navoGlass(backdrop, NavoShapes.GlassPill, glassStyle)
            .clip(NavoShapes.GlassPill)
            .clickable(onClickLabel = changeLabel, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .animateContentSize()
            .padding(start = 16.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = NavoIcons.Timer, contentDescription = null, tint = colors.accent, modifier = Modifier.size(TimerIconSize))
        Text(
            text = text,
            style = NavoTheme.typography.secondaryNumeric,
            color = colors.accent,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
private fun SleepTimer.accessibilityLabel(): String = when (this) {
    SleepTimer.Off -> stringResource(R.string.player_sleep_timer)
    SleepTimer.EndOfTrack -> stringResource(R.string.player_sleep_timer_active_end_of_track)
    is SleepTimer.Countdown -> {
        val minutes = ((remainingMs + MINUTE_MILLIS - 1) / MINUTE_MILLIS).toInt()
        if (remainingMs < MINUTE_MILLIS) {
            stringResource(R.string.player_sleep_timer_active_less_than_minute)
        } else {
            pluralStringResource(R.plurals.player_sleep_timer_active_minutes, minutes, minutes)
        }
    }
}

@Composable
private fun SleepTimerButtonPreviewContent(darkTheme: Boolean) {
    NavoTheme(darkTheme = darkTheme) {
        val backdrop = rememberNavoGlassBackdrop()
        Box(modifier = Modifier.background(NavoTheme.colors.background)) {
            NowPlayingBackdrop(track = previewNowPlayingState.track, modifier = Modifier.matchParentSize().navoGlassSource(backdrop))
            CompositionLocalProvider(LocalNavoGlassBackdrop provides backdrop) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SleepTimerButton(sleepTimer = SleepTimer.Off, onClick = {})
                    SleepTimerButton(sleepTimer = SleepTimer.Countdown(872_000, 900_000), onClick = {})
                    SleepTimerButton(sleepTimer = SleepTimer.EndOfTrack, onClick = {})
                }
            }
        }
    }
}

@Preview
@Composable
private fun SleepTimerButtonPreview() {
    SleepTimerButtonPreviewContent(darkTheme = true)
}

@Preview
@Composable
private fun SleepTimerButtonLightPreview() {
    SleepTimerButtonPreviewContent(darkTheme = false)
}
