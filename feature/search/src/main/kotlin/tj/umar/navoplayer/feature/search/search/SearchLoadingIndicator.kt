package tj.umar.navoplayer.feature.search.search

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.rememberMedallionRotation
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.search.R

private const val LOADER_ROTATION_MILLIS = 2_400
private const val LOADER_FADE_MILLIS = 200
private const val LOADER_DELAY_MILLIS = 300

@Composable
internal fun SearchLoadingIndicator(modifier: Modifier = Modifier) {
    val motion = rememberMotionEnabled()
    val rotation = rememberMedallionRotation(running = motion, periodMillis = LOADER_ROTATION_MILLIS)
    val alpha = remember { Animatable(0f) }
    val currentMotion by rememberUpdatedState(motion)
    LaunchedEffect(Unit) {
        delay(LOADER_DELAY_MILLIS.toLong())
        if (currentMotion) {
            alpha.animateTo(1f, tween(LOADER_FADE_MILLIS, easing = LinearOutSlowInEasing))
        } else {
            alpha.snapTo(1f)
        }
    }
    val description = stringResource(R.string.search_loading)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Medallion(
            palette = MedallionPalettes.all[2],
            rotationDegrees = rotation,
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer { this.alpha = alpha.value }
                .semantics { contentDescription = description },
        )
    }
}

@Preview(widthDp = 390, heightDp = 300)
@Composable
private fun SearchLoadingIndicatorPreview() {
    NavoTheme { SearchLoadingIndicator() }
}
