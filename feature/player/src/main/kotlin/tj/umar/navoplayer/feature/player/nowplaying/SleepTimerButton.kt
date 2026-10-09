package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.ui.format.formatDuration
import tj.umar.navoplayer.feature.player.R

private const val MINUTE_MILLIS = 60_000L

@Composable
internal fun SleepTimerButton(sleepTimer: SleepTimer, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NavoTheme.colors
    if (sleepTimer == SleepTimer.Off) {
        NavoIconButton(
            icon = NavoIcons.Timer,
            contentDescription = stringResource(R.string.player_sleep_timer),
            onClick = onClick,
            modifier = modifier,
            containerColor = colors.raised,
            size = 56.dp,
        )
        return
    }
    val label = sleepTimer.accessibilityLabel()
    val text = when (sleepTimer) {
        is SleepTimer.Countdown -> formatDuration(sleepTimer.remainingMs.ceilToSecond())
        else -> stringResource(R.string.player_sleep_timer_end_of_track_short)
    }
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .background(colors.raised)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = label }
            .animateContentSize()
            .padding(start = 16.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = NavoIcons.Timer, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
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

@Preview
@Composable
private fun SleepTimerButtonPreview() {
    NavoTheme {
        Row(
            modifier = Modifier.background(NavoTheme.colors.background).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SleepTimerButton(sleepTimer = SleepTimer.Off, onClick = {})
            SleepTimerButton(sleepTimer = SleepTimer.Countdown(872_000, 900_000), onClick = {})
            SleepTimerButton(sleepTimer = SleepTimer.EndOfTrack, onClick = {})
        }
    }
}
