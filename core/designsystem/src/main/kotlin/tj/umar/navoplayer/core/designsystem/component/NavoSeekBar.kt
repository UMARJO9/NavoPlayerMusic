package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private val SeekStrokeWidth = 4.dp
private val SeekCanvasHeight = 16.dp
private val SeekWaveAmplitude = 3.dp
private val SeekWavelength = 28.dp
private val SeekThumbSize = 16.dp
private const val WAVE_PERIOD_MILLIS = 1_600f
private const val NANOS_PER_MILLI = 1_000_000f
private const val AMPLITUDE_ANIMATION_MILLIS = 400
private const val THUMB_ACTIVE_SCALE = 1.4f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavoSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    playing: Boolean,
    stateDescription: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val dragged by interactionSource.collectIsDraggedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val touched = dragged || pressed
    val motionEnabled = rememberMotionEnabled()
    val waving = playing && enabled && !touched && motionEnabled && value > 0f
    val amplitude = animateDpAsState(
        targetValue = if (waving) SeekWaveAmplitude else 0.dp,
        animationSpec = tween(AMPLITUDE_ANIMATION_MILLIS),
        label = "seekWaveAmplitude",
    )
    val settling by remember { derivedStateOf { amplitude.value > 0.dp } }
    val phase = rememberWavePhase(running = waving || settling)
    val thumbScale by animateFloatAsState(
        targetValue = if (touched) THUMB_ACTIVE_SCALE else 1f,
        label = "seekThumbScale",
    )
    val activeColor = if (enabled) colors.accent else colors.contentMuted
    val thumbColor = if (enabled) colors.content else colors.contentMuted
    Slider(
        value = value.coerceIn(0f, 1f),
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        enabled = enabled,
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
                    .size(SeekThumbSize)
                    .graphicsLayer {
                        scaleX = thumbScale
                        scaleY = thumbScale
                    }
                    .background(thumbColor, CircleShape),
            )
        },
        track = { sliderState ->
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SeekCanvasHeight)
                    .drawWithCache {
                        val path = Path()
                        val wavelength = SeekWavelength.toPx()
                        val strokeWidth = SeekStrokeWidth.toPx()
                        onDrawBehind {
                            drawWavyTrack(
                                path = path,
                                fraction = sliderState.value,
                                amplitude = amplitude.value.toPx(),
                                phase = phase.floatValue,
                                wavelength = wavelength,
                                strokeWidth = strokeWidth,
                                activeColor = activeColor,
                                inactiveColor = colors.line,
                            )
                        }
                    },
            )
        },
    )
}

@Composable
private fun rememberMotionEnabled(): Boolean {
    var enabled by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val motionScale = coroutineContext[MotionDurationScale] ?: return@LaunchedEffect
        snapshotFlow { motionScale.scaleFactor > 0f }.collect { enabled = it }
    }
    return enabled
}

@Composable
private fun rememberWavePhase(running: Boolean): FloatState {
    val phase = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val elapsedMillis = (now - previous) / NANOS_PER_MILLI
                previous = now
                val advance = elapsedMillis / WAVE_PERIOD_MILLIS * WAVE_FULL_TURN
                phase.floatValue = (phase.floatValue + advance) % WAVE_FULL_TURN
            }
        }
    }
    return phase
}

@Preview
@Composable
private fun NavoSeekBarPreview() {
    NavoTheme {
        Column(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(20.dp),
        ) {
            NavoSeekBar(
                value = 0.38f,
                onValueChange = {},
                onValueChangeFinished = {},
                playing = true,
                stateDescription = "1:37 / 4:12",
                contentDescription = "Seek",
            )
            NavoSeekBar(
                value = 0.38f,
                onValueChange = {},
                onValueChangeFinished = {},
                playing = false,
                stateDescription = "1:37 / 4:12",
                contentDescription = "Seek",
            )
        }
    }
}
