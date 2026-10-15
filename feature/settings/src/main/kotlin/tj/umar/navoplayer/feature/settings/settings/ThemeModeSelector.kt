package tj.umar.navoplayer.feature.settings.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.ThemeSwatch
import tj.umar.navoplayer.core.designsystem.component.ThemeSwatchVariant
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.feature.settings.R

private val ThemeOptionShape = RoundedCornerShape(20.dp)
private val ThemeOptionMinHeight = 48.dp

@Composable
internal fun ThemeModeSelector(
    options: List<ThemeMode>,
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.ScreenHorizontal)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        options.forEach { mode ->
            ThemeModeOption(
                mode = mode,
                selected = mode == selected,
                enabled = enabled,
                onClick = { onSelect(mode) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    mode: ThemeMode,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    Column(
        modifier = modifier
            .heightIn(min = ThemeOptionMinHeight)
            .clip(ThemeOptionShape)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemeSwatch(variant = mode.swatchVariant(), selected = selected, modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(mode.labelRes()),
            style = if (selected) NavoTheme.typography.labelStrong else NavoTheme.typography.label,
            color = if (selected) colors.accent else colors.contentMuted,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun ThemeMode.swatchVariant(): ThemeSwatchVariant = when (this) {
    ThemeMode.System -> ThemeSwatchVariant.Split
    ThemeMode.Light -> ThemeSwatchVariant.Light
    ThemeMode.Dark -> ThemeSwatchVariant.Dark
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.System -> R.string.settings_theme_system
    ThemeMode.Light -> R.string.settings_theme_light
    ThemeMode.Dark -> R.string.settings_theme_dark
}

@Composable
private fun ThemeModeSelectorSample() {
    Column(modifier = Modifier.background(NavoTheme.colors.background).padding(vertical = 16.dp)) {
        ThemeModeSelector(
            options = SettingsState().themeOptions,
            selected = ThemeMode.Dark,
            onSelect = {},
        )
    }
}

@Preview(name = "Dark", widthDp = 390)
@Composable
private fun ThemeModeSelectorDarkPreview() {
    NavoTheme { ThemeModeSelectorSample() }
}

@Preview(name = "Light", widthDp = 390)
@Composable
private fun ThemeModeSelectorLightPreview() {
    NavoTheme(darkTheme = false) { ThemeModeSelectorSample() }
}
