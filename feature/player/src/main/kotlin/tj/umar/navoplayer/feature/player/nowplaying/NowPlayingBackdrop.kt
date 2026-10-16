package tj.umar.navoplayer.feature.player.nowplaying

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalette
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoColors
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.artworkKey
import tj.umar.navoplayer.core.ui.artwork.rememberArtwork

private val ArtworkSize = 300.dp
private val ArtworkBlur = 56.dp
private const val ARTWORK_SCALE = 1.4f
private const val ARTWORK_ALPHA = 0.55f
private const val CROSSFADE_MILLIS = 600
private const val PRIMARY_GLOW_ALPHA = 0.45f
private const val SECONDARY_GLOW_ALPHA = 0.28f
private const val TERTIARY_GLOW_ALPHA = 0.22f
private const val LIGHT_GLOW_FACTOR = 0.7f

private val DarkScrimStops = listOf(0f to 0.50f, 0.14f to 0.28f, 0.42f to 0.32f, 0.60f to 0.62f, 0.80f to 0.72f, 1f to 0.62f)
private val LightScrimStops = listOf(0f to 0.62f, 0.14f to 0.42f, 0.42f to 0.45f, 0.60f to 0.75f, 0.80f to 0.84f, 1f to 0.78f)

@Composable
internal fun NowPlayingBackdrop(track: Track?, modifier: Modifier = Modifier) {
    val colors = NavoTheme.colors
    val motionEnabled = rememberMotionEnabled()
    val auraTrack = remember(track?.id) { track }
    Box(
        modifier = modifier
            .clipToBounds()
            .background(colors.background),
    ) {
        Crossfade(
            targetState = auraTrack,
            modifier = Modifier.fillMaxSize(),
            animationSpec = if (motionEnabled) tween<Float>(CROSSFADE_MILLIS) else snap<Float>(),
            label = "nowPlayingBackdrop",
        ) { shownTrack ->
            if (shownTrack != null) TrackAura(track = shownTrack, colors = colors)
        }
        val scrim = remember(colors) { scrimBrush(colors) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrim),
        )
    }
}

@Composable
private fun TrackAura(track: Track, colors: NavoColors) {
    val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
    val glowFactor = if (colors.isDark) 1f else LIGHT_GLOW_FACTOR
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawGlows(palette, glowFactor) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val artwork = rememberArtwork(track.artworkKey, ArtworkSize).value
            if (artwork != null) {
                Image(
                    bitmap = artwork,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = ARTWORK_SCALE
                            scaleY = ARTWORK_SCALE
                        }
                        .blur(ArtworkBlur)
                        .alpha(ARTWORK_ALPHA),
                )
            }
        }
    }
}

private fun DrawScope.drawGlows(palette: MedallionPalette, factor: Float) {
    val width = size.width
    val height = size.height
    drawGlow(palette.background, PRIMARY_GLOW_ALPHA * factor, Offset(width * 0.3f, height * 0.22f), width * 0.95f)
    drawGlow(palette.petals, SECONDARY_GLOW_ALPHA * factor, Offset(width * 0.9f, height * 0.42f), width * 0.75f)
    drawGlow(palette.center, TERTIARY_GLOW_ALPHA * factor, Offset(width * 0.15f, height * 0.88f), width * 0.9f)
}

private fun DrawScope.drawGlow(color: Color, alpha: Float, center: Offset, radius: Float) {
    if (radius <= 0f) return
    drawRect(
        Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
    )
}

private fun scrimBrush(colors: NavoColors): Brush {
    val stops = if (colors.isDark) DarkScrimStops else LightScrimStops
    return Brush.verticalGradient(
        *stops.map { (position, alpha) -> position to colors.background.copy(alpha = alpha) }.toTypedArray(),
    )
}
