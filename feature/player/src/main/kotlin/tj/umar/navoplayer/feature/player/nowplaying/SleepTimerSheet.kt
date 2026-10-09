package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.designsystem.component.SettingsInfoRow
import tj.umar.navoplayer.core.designsystem.component.SettingsRadioRow
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.ui.format.formatDuration
import tj.umar.navoplayer.feature.player.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SleepTimerSheet(
    sleepTimer: SleepTimer,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = { onIntent(NowPlayingIntent.SleepTimerSheetDismissed) },
        sheetState = sheetState,
        modifier = modifier,
        containerColor = NavoTheme.colors.raised,
        contentColor = NavoTheme.colors.content,
    ) {
        SleepTimerSheetContent(
            sleepTimer = sleepTimer,
            onIntent = { intent ->
                scope.launch { sheetState.hide() }.invokeOnCompletion { onIntent(intent) }
            },
        )
    }
}

@Composable
internal fun SleepTimerSheetContent(
    sleepTimer: SleepTimer,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    Column(modifier = modifier.fillMaxWidth().padding(bottom = NavoSpacing.Large)) {
        Text(
            text = stringResource(R.string.player_sleep_timer),
            style = NavoTheme.typography.titleS,
            color = colors.content,
            modifier = Modifier
                .padding(horizontal = NavoSpacing.ScreenHorizontal)
                .semantics { heading() },
        )
        val status = when (sleepTimer) {
            SleepTimer.Off -> null
            SleepTimer.EndOfTrack -> stringResource(R.string.player_sleep_timer_end_of_track)
            is SleepTimer.Countdown ->
                stringResource(R.string.player_sleep_timer_remaining, formatDuration(sleepTimer.remainingMs.ceilToSecond()))
        }
        if (status != null) {
            Text(
                text = status,
                style = NavoTheme.typography.secondary,
                color = colors.accent,
                modifier = Modifier.padding(horizontal = NavoSpacing.ScreenHorizontal, vertical = NavoSpacing.ExtraSmall),
            )
        }
        val selected = sleepTimer.selectedOption()
        Column(modifier = Modifier.padding(top = NavoSpacing.Small).selectableGroup()) {
            SleepTimerOption.presets.forEach { option ->
                SettingsRadioRow(
                    title = option.label(),
                    selected = option == selected,
                    onClick = { onIntent(NowPlayingIntent.SleepTimerOptionSelected(option)) },
                )
            }
        }
        if (sleepTimer != SleepTimer.Off) {
            SettingsInfoRow(
                title = stringResource(R.string.player_sleep_timer_turn_off),
                modifier = Modifier.clickable(role = Role.Button) { onIntent(NowPlayingIntent.SleepTimerCancelClicked) },
            )
        }
    }
}

@Composable
private fun SleepTimerOption.label(): String = when (this) {
    is SleepTimerOption.Minutes -> pluralStringResource(R.plurals.player_sleep_timer_minutes, minutes, minutes)
    SleepTimerOption.EndOfTrack -> stringResource(R.string.player_sleep_timer_end_of_track)
}

internal fun Long.ceilToSecond(): Long = (this + 999) / 1000 * 1000

@Preview(widthDp = 390)
@Composable
private fun SleepTimerOffPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.raised).padding(top = 16.dp)) {
            SleepTimerSheetContent(sleepTimer = SleepTimer.Off, onIntent = {})
        }
    }
}

@Preview(widthDp = 390)
@Composable
private fun SleepTimerCountdownPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.raised).padding(top = 16.dp)) {
            SleepTimerSheetContent(sleepTimer = SleepTimer.Countdown(872_000, 900_000), onIntent = {})
        }
    }
}

@Preview(widthDp = 390)
@Composable
private fun SleepTimerEndOfTrackPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.raised).padding(top = 16.dp)) {
            SleepTimerSheetContent(sleepTimer = SleepTimer.EndOfTrack, onIntent = {})
        }
    }
}
