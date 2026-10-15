package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val FOCUS_MILLIS = 180
private const val FADE_IN_MILLIS = 150
private const val PLACEHOLDER_FADE_OUT_MILLIS = 90
private const val CLEAR_FADE_OUT_MILLIS = 100
private const val CLEAR_INITIAL_SCALE = 0.6f
private const val FOCUSED_OUTLINE_ALPHA = 0.55f
private val OutlineWidth = 1.5.dp

@Composable
fun NavoSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    clearContentDescription: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onSearch: () -> Unit = {},
) {
    val colors = NavoTheme.colors
    val textStyle = NavoTheme.typography.itemTitle.copy(color = colors.content)
    val motion = rememberMotionEnabled()
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusSpec: AnimationSpec<Color> = if (motion) tween(FOCUS_MILLIS, easing = FastOutSlowInEasing) else snap()
    val outlineColor: State<Color> = animateColorAsState(
        targetValue = if (focused) colors.accent.copy(alpha = FOCUSED_OUTLINE_ALPHA) else Color.Transparent,
        animationSpec = focusSpec,
        label = "searchOutline",
    )
    val iconTint by animateColorAsState(
        targetValue = if (focused) colors.accent else colors.contentSecondary,
        animationSpec = focusSpec,
        label = "searchIconTint",
    )
    Row(
        modifier = modifier
            .height(48.dp)
            .drawWithCache {
                val strokeWidth = OutlineWidth.toPx()
                val inset = strokeWidth / 2f
                val outline = NavoShapes.Pill.createOutline(
                    Size(size.width - strokeWidth, size.height - strokeWidth),
                    layoutDirection,
                    this,
                )
                val stroke = Stroke(strokeWidth)
                onDrawWithContent {
                    drawContent()
                    val color = outlineColor.value
                    if (color.alpha > 0f) {
                        translate(inset, inset) { drawOutline(outline, color, style = stroke) }
                    }
                }
            }
            .clip(NavoShapes.Pill)
            .background(colors.raised)
            .padding(start = 14.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = NavoIcons.Search,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            SearchPlaceholder(
                visible = value.isEmpty(),
                text = placeholder,
                style = textStyle,
                motion = motion,
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }
        AnimatedVisibility(
            visible = value.isNotEmpty(),
            enter = if (motion) {
                fadeIn(tween(FADE_IN_MILLIS)) +
                    scaleIn(tween(FADE_IN_MILLIS, easing = FastOutSlowInEasing), initialScale = CLEAR_INITIAL_SCALE)
            } else {
                EnterTransition.None
            },
            exit = if (motion) {
                fadeOut(tween(CLEAR_FADE_OUT_MILLIS)) +
                    scaleOut(tween(CLEAR_FADE_OUT_MILLIS, easing = FastOutLinearInEasing), targetScale = CLEAR_INITIAL_SCALE)
            } else {
                ExitTransition.None
            },
            label = "searchClear",
        ) {
            NavoIconButton(
                icon = NavoIcons.Close,
                contentDescription = clearContentDescription,
                onClick = onClear,
                iconSize = 20.dp,
            )
        }
    }
}

@Composable
private fun SearchPlaceholder(visible: Boolean, text: String, style: TextStyle, motion: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = if (motion) fadeIn(tween(FADE_IN_MILLIS)) else EnterTransition.None,
        exit = if (motion) fadeOut(tween(PLACEHOLDER_FADE_OUT_MILLIS)) else ExitTransition.None,
        label = "searchPlaceholder",
    ) {
        Text(
            text = text,
            style = style,
            color = NavoTheme.colors.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NavoSearchFieldSample() {
    Column(
        modifier = Modifier
            .background(NavoTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NavoSearchField(
            value = "",
            onValueChange = {},
            placeholder = "Треки, альбомы, исполнители",
            clearContentDescription = "Очистить поиск",
            onClear = {},
        )
        NavoSearchField(
            value = "Ҷавонӣ",
            onValueChange = {},
            placeholder = "Треки, альбомы, исполнители",
            clearContentDescription = "Очистить поиск",
            onClear = {},
        )
    }
}

@Preview
@Composable
private fun NavoSearchFieldPreview() {
    NavoTheme { NavoSearchFieldSample() }
}

@Preview
@Composable
private fun NavoSearchFieldLightPreview() {
    NavoTheme(darkTheme = false) { NavoSearchFieldSample() }
}
