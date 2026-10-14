package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.KhotamStar
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

val SettingsGroupShape: Shape = RoundedCornerShape(20.dp)
private val SettingsIconTileSize = 36.dp
private val SettingsIconSize = 20.dp
private val SettingsRowOuterInset = 8.dp
private val SettingsRowInnerPadding = 12.dp
private val SettingsGroupRowInnerPadding = 16.dp
private val SettingsRowSpacing = 14.dp
private val SettingsHeaderStarSize = 10.dp
private const val SETTINGS_HEADER_LINE_ALPHA = 0.45f

val SettingsGroupDividerInset: Dp = SettingsGroupRowInnerPadding + SettingsIconTileSize + SettingsRowSpacing

val LocalSettingsRowInGroup = staticCompositionLocalOf { false }

@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    val colors = NavoTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = NavoSpacing.ScreenHorizontal, end = NavoSpacing.ScreenHorizontal, top = 24.dp, bottom = 10.dp)
            .semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        KhotamStar(size = SettingsHeaderStarSize, color = colors.ornament)
        Text(
            text = title,
            style = NavoTheme.typography.labelStrong.copy(letterSpacing = 0.06.em),
            color = colors.accent,
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(colors.ornament.copy(alpha = SETTINGS_HEADER_LINE_ALPHA), Color.Transparent),
                    ),
                ),
        )
    }
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = NavoTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.ScreenHorizontal)
            .clip(SettingsGroupShape)
            .background(colors.card)
            .border(1.dp, colors.divider, SettingsGroupShape),
    ) {
        CompositionLocalProvider(LocalSettingsRowInGroup provides true) {
            content()
        }
    }
}

@Composable
fun SettingsGroupDivider(
    modifier: Modifier = Modifier,
    startInset: Dp = SettingsGroupDividerInset,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startInset)
            .height(1.dp)
            .background(NavoTheme.colors.divider),
    )
}

@Composable
fun SettingsIconTile(
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = NavoTheme.colors.onTile,
) {
    Box(
        modifier = modifier
            .size(SettingsIconTileSize)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(SettingsIconSize),
        )
    }
}

@Composable
fun SettingsNavigationRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    SettingsRowLayout(
        title = title,
        subtitle = subtitle,
        leading = leading,
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
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = NavoTheme.colors
    SettingsRowLayout(
        title = title,
        subtitle = subtitle,
        leading = leading,
        modifier = modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.contentSecondary,
                uncheckedTrackColor = Color.Transparent,
                uncheckedBorderColor = colors.contentSecondary,
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
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = NavoTheme.colors
    SettingsRowLayout(
        title = title,
        subtitle = subtitle,
        leading = leading,
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
fun SettingsInfoRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    SettingsRowLayout(title = title, subtitle = subtitle, leading = leading, modifier = modifier) {}
}

@Composable
private fun SettingsRowLayout(
    title: String,
    subtitle: String?,
    leading: (@Composable () -> Unit)?,
    modifier: Modifier,
    trailing: @Composable () -> Unit,
) {
    val inGroup = LocalSettingsRowInGroup.current
    val outer = if (inGroup) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsRowOuterInset)
            .clip(NavoShapes.TrackRow)
    }
    Row(
        modifier = outer
            .then(modifier)
            .heightIn(min = 60.dp)
            .padding(
                horizontal = if (inGroup) SettingsGroupRowInnerPadding else SettingsRowInnerPadding,
                vertical = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SettingsRowSpacing),
    ) {
        leading?.invoke()
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

@Composable
private fun SettingsRowsSample() {
    val colors = NavoTheme.colors
    Column(
        modifier = Modifier
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        SettingsSectionHeader(title = "Воспроизведение")
        SettingsGroup {
            SettingsSwitchRow(
                title = "Пауза при отключении наушников",
                subtitle = "Проводных и Bluetooth",
                checked = true,
                onCheckedChange = {},
                leading = { SettingsIconTile(icon = NavoIcons.Headphones, containerColor = colors.crimson) },
            )
            SettingsGroupDivider()
            SettingsNavigationRow(
                title = "Эквалайзер",
                subtitle = "Выключен",
                onClick = {},
                leading = {
                    SettingsIconTile(icon = NavoIcons.Equalizer, containerColor = colors.accent, contentColor = colors.onAccent)
                },
            )
        }
        SettingsSectionHeader(title = "Сортировка")
        Column(modifier = Modifier.selectableGroup()) {
            SettingsRadioRow(title = "Название", selected = true, onClick = {})
            SettingsRadioRow(title = "Исполнитель", selected = false, onClick = {})
        }
        SettingsNavigationRow(title = "Скрытые папки", subtitle = "Нет", onClick = {})
        SettingsInfoRow(title = "Navo", subtitle = "Версия 1.0")
    }
}

@Preview(name = "Dark", widthDp = 390)
@Composable
private fun SettingsRowsDarkPreview() {
    NavoTheme { SettingsRowsSample() }
}

@Preview(name = "Light", widthDp = 390)
@Composable
private fun SettingsRowsLightPreview() {
    NavoTheme(darkTheme = false) { SettingsRowsSample() }
}
