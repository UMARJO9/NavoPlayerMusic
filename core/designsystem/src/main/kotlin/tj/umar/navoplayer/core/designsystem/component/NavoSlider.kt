package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private val TrackHeight = 4.dp
private val ThumbSize = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavoSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    stateDescription: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Slider(
        value = value.coerceIn(0f, 1f),
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.stateDescription = stateDescription
                this.contentDescription = contentDescription
            },
        thumb = {
            Box(
                modifier = Modifier
                    .size(ThumbSize)
                    .background(colors.content, CircleShape),
            )
        },
        track = { sliderState ->
            val fraction = sliderState.value.coerceIn(0f, 1f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TrackHeight),
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
                    color = colors.accent,
                    start = Offset(0f, centerY),
                    end = Offset(size.width * fraction, centerY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        },
    )
}

@Preview
@Composable
private fun NavoSliderPreview() {
    NavoTheme {
        Box(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(20.dp),
        ) {
            NavoSlider(
                value = 0.38f,
                onValueChange = {},
                onValueChangeFinished = {},
                stateDescription = "1:37 из 4:12",
                contentDescription = "Позиция в треке",
            )
        }
    }
}
