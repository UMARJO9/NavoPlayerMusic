package tj.umar.navoplayer.feature.settings.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoChip
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.SettingsInfoRow
import tj.umar.navoplayer.core.designsystem.component.SettingsNavigationRow
import tj.umar.navoplayer.core.designsystem.component.SettingsSectionHeader
import tj.umar.navoplayer.core.designsystem.component.SettingsSwitchRow
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.feature.settings.R

@Composable
internal fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        SettingsTopBar(title = stringResource(R.string.settings_title), onBack = { onIntent(SettingsIntent.BackClicked) })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
        ) {
            item(key = "library") { SettingsSectionHeader(title = stringResource(R.string.settings_section_library)) }
            item(key = "duration") { DurationSetting(state = state, onIntent = onIntent) }
            item(key = "folders") {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_hidden_folders_title),
                    subtitle = if (state.hiddenFolderCount == 0) {
                        stringResource(R.string.settings_hidden_folders_none)
                    } else {
                        pluralStringResource(R.plurals.settings_hidden_folders_count, state.hiddenFolderCount, state.hiddenFolderCount)
                    },
                    onClick = { onIntent(SettingsIntent.HiddenFoldersClicked) },
                )
            }
            item(key = "playback") { SettingsSectionHeader(title = stringResource(R.string.settings_section_playback)) }
            item(key = "headphones") {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_pause_on_disconnect_title),
                    subtitle = stringResource(R.string.settings_pause_on_disconnect_subtitle),
                    checked = state.pauseOnHeadphonesDisconnect,
                    onCheckedChange = { onIntent(SettingsIntent.PauseOnHeadphonesDisconnectToggled(it)) },
                )
            }
            item(key = "about") { SettingsSectionHeader(title = stringResource(R.string.settings_section_about)) }
            item(key = "app") {
                SettingsInfoRow(
                    title = stringResource(R.string.settings_about_app_name),
                    subtitle = stringResource(R.string.settings_about_version, state.versionName) + "\n" +
                        stringResource(R.string.settings_about_description),
                )
            }
            item(key = "licenses") {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_licenses_title),
                    onClick = { onIntent(SettingsIntent.LicensesClicked) },
                )
            }
        }
    }
}

@Composable
internal fun SettingsTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = NavoSpacing.ScreenHorizontal, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        NavoIconButton(
            icon = NavoIcons.ChevronLeft,
            contentDescription = stringResource(R.string.settings_back),
            onClick = onBack,
        )
        Text(
            text = title,
            style = NavoTheme.typography.displayM,
            color = NavoTheme.colors.content,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun DurationSetting(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsInfoRow(
            title = stringResource(R.string.settings_min_duration_title),
            subtitle = stringResource(R.string.settings_min_duration_subtitle),
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            contentPadding = PaddingValues(horizontal = NavoSpacing.ScreenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small),
        ) {
            items(state.durationOptions, key = { it.name }) { option ->
                NavoChip(
                    label = option.label(),
                    selected = option == state.minTrackDuration,
                    onClick = { onIntent(SettingsIntent.MinTrackDurationSelected(option)) },
                )
            }
        }
    }
}

@Composable
private fun MinTrackDuration.label(): String =
    if (this == MinTrackDuration.Off) {
        stringResource(R.string.settings_min_duration_off)
    } else {
        stringResource(R.string.settings_min_duration_seconds, seconds)
    }

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SettingsPreview() {
    NavoTheme {
        SettingsScreen(
            state = SettingsState(
                isLoading = false,
                minTrackDuration = MinTrackDuration.ThirtySeconds,
                hiddenFolderCount = 2,
                versionName = "1.0",
            ),
            onIntent = {},
        )
    }
}
