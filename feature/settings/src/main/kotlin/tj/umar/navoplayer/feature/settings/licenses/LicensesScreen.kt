package tj.umar.navoplayer.feature.settings.licenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.SettingsInfoRow
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.settings.R
import tj.umar.navoplayer.feature.settings.settings.SettingsTopBar

@Composable
internal fun LicensesScreen(
    state: LicensesState,
    onIntent: (LicensesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        SettingsTopBar(
            title = stringResource(R.string.settings_licenses_title),
            onBack = { onIntent(LicensesIntent.BackClicked) },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
        ) {
            items(state.entries, key = { it.nameRes }) { entry ->
                SettingsInfoRow(title = stringResource(entry.nameRes), subtitle = stringResource(entry.licenseRes))
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LicensesPreview() {
    NavoTheme {
        LicensesScreen(state = LicensesState(), onIntent = {})
    }
}
