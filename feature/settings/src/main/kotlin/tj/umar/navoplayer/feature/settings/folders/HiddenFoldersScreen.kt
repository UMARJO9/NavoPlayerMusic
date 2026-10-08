package tj.umar.navoplayer.feature.settings.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.SettingsSwitchRow
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.LibraryFolder
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.feature.settings.R
import tj.umar.navoplayer.feature.settings.settings.SettingsTopBar

@Composable
internal fun HiddenFoldersScreen(
    state: HiddenFoldersState,
    onIntent: (HiddenFoldersIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        SettingsTopBar(
            title = stringResource(R.string.settings_hidden_folders_title),
            onBack = { onIntent(HiddenFoldersIntent.BackClicked) },
        )
        PhasedContent(
            phase = when {
                state.isLoading -> ContentPhase.Loading
                state.loadFailed -> ContentPhase.Error
                state.folders.isEmpty() -> ContentPhase.Empty
                else -> ContentPhase.Content
            },
            modifier = Modifier.fillMaxSize(),
            empty = {
                StateMessage(message = stringResource(R.string.settings_folders_empty), palette = MedallionPalettes.all[2])
            },
            error = {
                StateMessage(
                    message = stringResource(R.string.settings_folders_error),
                    palette = MedallionPalettes.all[0],
                    actionLabel = stringResource(R.string.settings_folders_retry),
                    onAction = { onIntent(HiddenFoldersIntent.RetryLoad) },
                )
            },
        ) {
            FolderList(folders = state.folders, onIntent = onIntent)
        }
    }
}

@Composable
private fun FolderList(folders: List<LibraryFolder>, onIntent: (HiddenFoldersIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
    ) {
        item(key = "hint") {
            Text(
                text = stringResource(R.string.settings_folders_hint),
                style = NavoTheme.typography.secondary,
                color = NavoTheme.colors.contentSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NavoSpacing.ScreenHorizontal, vertical = NavoSpacing.Small),
            )
        }
        items(folders, key = { it.path }) { folder ->
            SettingsSwitchRow(
                title = folder.name,
                subtitle = stringResource(
                    CoreUiR.string.core_ui_group_subtitle,
                    folder.path,
                    pluralStringResource(CoreUiR.plurals.core_ui_track_count, folder.trackCount, folder.trackCount),
                ),
                checked = !folder.isExcluded,
                onCheckedChange = { visible -> onIntent(HiddenFoldersIntent.FolderVisibilityToggled(folder.path, visible)) },
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun HiddenFoldersPreview() {
    NavoTheme {
        HiddenFoldersScreen(
            state = HiddenFoldersState(
                isLoading = false,
                folders = listOf(
                    LibraryFolder("Music/Navo", "Navo", 12, isExcluded = false),
                    LibraryFolder("Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio", "WhatsApp Audio", 48, isExcluded = true),
                ),
            ),
            onIntent = {},
        )
    }
}
