package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = NavoTheme.typography.label,
        color = NavoTheme.colors.accent,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = NavoSpacing.ScreenHorizontal, end = NavoSpacing.ScreenHorizontal, top = 20.dp, bottom = 6.dp)
            .semantics { heading() },
    )
}

@Composable
fun SettingsNavigationRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    SettingsRowLayout(
        title = title,
        subtitle = subtitle,
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Icon(
            imageVector = NavoIcons.ChevronRight,
            contentDescription = null,
            tint = NavoTheme.colors.contentSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = NavoTheme.colors
    SettingsRowLayout(
        title = title,
        subtitle = subtitle,
        modifier = modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.contentSecondary,
                uncheckedTrackColor = colors.raised,
                uncheckedBorderColor = colors.line,
            ),
        )
    }
}

@Composable
fun SettingsRadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = NavoTheme.colors
    SettingsRowLayout(
        title = title,
        subtitle = subtitle,
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.accent,
                unselectedColor = colors.contentSecondary,
            ),
        )
    }
}

@Composable
fun SettingsInfoRow(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    SettingsRowLayout(title = title, subtitle = subtitle, modifier = modifier) {}
}

@Composable
private fun SettingsRowLayout(
    title: String,
    subtitle: String?,
    modifier: Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(NavoShapes.TrackRow)
            .then(modifier)
            .heightIn(min = 60.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = NavoTheme.typography.itemTitle,
                color = NavoTheme.colors.content,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = NavoTheme.typography.secondary,
                    color = NavoTheme.colors.contentSecondary,
                )
            }
        }
        trailing()
    }
}

@Preview(widthDp = 390)
@Composable
private fun SettingsRowsPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.background)) {
            SettingsSectionHeader(title = "Воспроизведение")
            SettingsSwitchRow(
                title = "Пауза при отключении наушников",
                subtitle = "Проводных и Bluetooth",
                checked = true,
                onCheckedChange = {},
            )
            SettingsNavigationRow(title = "Скрытые папки", subtitle = "Нет", onClick = {})
            SettingsRadioRow(title = "Название", selected = true, onClick = {})
            SettingsInfoRow(title = "Navo", subtitle = "Версия 1.0")
        }
    }
}
