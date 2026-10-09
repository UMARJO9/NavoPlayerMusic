package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.modifier.pressScale
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val DISABLED_ALPHA = 0.4f
private const val ICON_SWAP_MILLIS = 220
private const val ICON_SWAP_SCALE = 0.6f

@Composable
fun NavoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    height: Dp = 56.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
    textStyle: TextStyle = NavoTheme.typography.labelStrong,
    enabled: Boolean = true,
) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressScale(interactionSource)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .heightIn(min = height)
            .clip(NavoShapes.Pill)
            .background(colors.accent)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.onAccent),
                role = Role.Button,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = text,
            style = textStyle,
            color = colors.onAccent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun NavoIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    tint: Color = NavoTheme.colors.content,
) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScale(interactionSource, pressedScale = 0.9f)
            .size(size)
            .clip(NavoShapes.Pill)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.content),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = icon,
            transitionSpec = {
                val spec = tween<Float>(ICON_SWAP_MILLIS)
                val enter = scaleIn(spec, initialScale = ICON_SWAP_SCALE) + fadeIn(spec)
                val exit = scaleOut(spec, targetScale = ICON_SWAP_SCALE) + fadeOut(spec)
                enter togetherWith exit
            },
            label = "iconSwap",
        ) { shownIcon ->
            Icon(
                imageVector = shownIcon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Preview
@Composable
private fun NavoButtonsPreview() {
    NavoTheme {
        Column(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NavoButton(text = "Разрешить доступ к музыке", onClick = {})
            NavoButton(
                text = "Перемешать",
                onClick = {},
                leadingIcon = NavoIcons.Shuffle,
                height = 44.dp,
                contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavoIconButton(icon = NavoIcons.Search, contentDescription = "Поиск", onClick = {})
                NavoIconButton(
                    icon = NavoIcons.Sort,
                    contentDescription = "Сортировка",
                    onClick = {},
                    containerColor = NavoTheme.colors.raised,
                )
            }
        }
    }
}
