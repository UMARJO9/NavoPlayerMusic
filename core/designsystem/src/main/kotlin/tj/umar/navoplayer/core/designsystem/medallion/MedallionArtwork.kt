package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val ARTWORK_FADE_MILLIS = 220
private val ArtworkRimWidth = 1.dp

@Composable
fun MedallionArtwork(
    palette: MedallionPalette,
    image: ImageBitmap?,
    modifier: Modifier = Modifier,
    variant: MedallionVariant? = null,
    rotationDegrees: () -> Float = { 0f },
    rim: Color = NavoTheme.colors.medallionRim,
) {
    val motionEnabled by rememberUpdatedState(rememberMotionEnabled())
    val fade = remember { Animatable(if (image != null) 1f else 0f) }
    val imageOpaque by remember { derivedStateOf { fade.value >= 1f } }

    LaunchedEffect(image) {
        when {
            image == null -> fade.snapTo(0f)
            fade.value >= 1f -> Unit
            motionEnabled -> fade.animateTo(1f, tween(ARTWORK_FADE_MILLIS))
            else -> fade.snapTo(1f)
        }
    }

    Box(modifier = modifier) {
        if (image == null || !imageOpaque) {
            Medallion(
                palette = palette,
                modifier = Modifier.matchParentSize(),
                variant = variant,
                rotationDegrees = rotationDegrees,
                rim = rim,
            )
        }
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        rotationZ = rotationDegrees()
                        alpha = fade.value
                    }
                    .clip(CircleShape)
                    .drawWithContent {
                        drawContent()
                        if (rim.alpha > 0f) {
                            val width = ArtworkRimWidth.toPx()
                            drawCircle(rim, radius = size.minDimension / 2f - width / 2f, style = Stroke(width))
                        }
                    },
            )
        }
    }
}

private fun solidImage(color: Color): ImageBitmap {
    val image = ImageBitmap(64, 64)
    Canvas(image).drawRect(0f, 0f, 64f, 64f, Paint().apply { this.color = color })
    return image
}

@Preview(widthDp = 260, heightDp = 140)
@Composable
private fun MedallionArtworkPreview() {
    NavoTheme {
        Row(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MedallionArtwork(
                palette = MedallionPalettes.forKey(1L),
                image = null,
                modifier = Modifier.size(104.dp),
            )
            MedallionArtwork(
                palette = MedallionPalettes.forKey(2L),
                image = solidImage(Color(0xFF8C3B2E)),
                modifier = Modifier.size(104.dp),
            )
        }
    }
}
