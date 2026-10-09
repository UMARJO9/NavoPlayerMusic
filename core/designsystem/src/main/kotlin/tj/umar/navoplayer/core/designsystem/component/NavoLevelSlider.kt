package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import kotlin.math.roundToInt

private val LevelTrackHeight = 4.dp
private val LevelThumbSize = 18.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavoLevelSlider(
    value: Int,
    valueRange: IntRange,
    step: Int,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: () -> Unit,
    stateDescription: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val activeColor = if (enabled) colors.accent else colors.contentMuted
    val thumbColor = if (enabled) colors.content else colors.contentMuted
    val start = valueRange.first.toFloat()
    val end = valueRange.last.toFloat()
    val steps = ((valueRange.last - valueRange.first) / step.coerceAtLeast(1) - 1).coerceAtLeast(0)
    Slider(
        value = value.toFloat().coerceIn(start, end),
        onValueChange = { raw ->
            val snapped = valueRange.first + ((raw - start) / step).roundToInt() * step
            onValueChange(snapped.coerceIn(valueRange))
        },
        onValueChangeFinished = onValueChangeFinished,
        valueRange = start..end,
        steps = steps,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = SliderDefaults.colors(activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.stateDescription = stateDescription
                this.contentDescription = contentDescription
            },
        thumb = {
            Box(
                modifier = Modifier
                    .size(LevelThumbSize)
                    .background(thumbColor, CircleShape),
            )
        },
        track = { sliderState ->
            val span = (end - start).takeIf { it > 0f } ?: 1f
            val zero = ((0f - start) / span).coerceIn(0f, 1f)
            val fraction = ((sliderState.value - start) / span).coerceIn(0f, 1f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LevelTrackHeight),
            ) {
                val centerY = size.height / 2f
                val stroke = size.height
                drawLine(
                    color = colors.line,
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = activeColor,
                    start = Offset(size.width * minOf(zero, fraction), centerY),
                    end = Offset(size.width * maxOf(zero, fraction), centerY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        },
    )
}

@Preview
@Composable
private fun NavoLevelSliderPreview() {
    NavoTheme {
        Column(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(20.dp),
        ) {
            NavoLevelSlider(
                value = 600,
                valueRange = -1500..1500,
                step = 100,
                onValueChange = {},
                onValueChangeFinished = {},
                stateDescription = "+6 дБ",
                contentDescription = "Полоса 230 Гц",
            )
            NavoLevelSlider(
                value = -900,
                valueRange = -1500..1500,
                step = 100,
                onValueChange = {},
                onValueChangeFinished = {},
                stateDescription = "−9 дБ",
                contentDescription = "Полоса 3.6 кГц",
                enabled = false,
            )
        }
    }
}
