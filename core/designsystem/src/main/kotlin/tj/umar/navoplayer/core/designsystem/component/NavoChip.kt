package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.modifier.pressScale
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val CHIP_COLOR_DURATION_MILLIS = 200

@Composable
fun NavoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.raised,
        animationSpec = tween(CHIP_COLOR_DURATION_MILLIS),
        label = "chipContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onAccent else colors.contentMuted,
        animationSpec = tween(CHIP_COLOR_DURATION_MILLIS),
        label = "chipContent",
    )
    Box(
        modifier = modifier
            .pressScale(interactionSource)
            .height(40.dp)
            .clip(NavoShapes.Pill)
            .background(container)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = ripple(color = content),
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = NavoTheme.typography.label, color = content, maxLines = 1)
    }
}

@Preview
@Composable
private fun NavoChipPreview() {
    NavoTheme {
        Row(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NavoChip(label = "Треки", selected = true, onClick = {})
            NavoChip(label = "Альбомы", selected = false, onClick = {})
            NavoChip(label = "Исполнители", selected = false, onClick = {})
        }
    }
}
