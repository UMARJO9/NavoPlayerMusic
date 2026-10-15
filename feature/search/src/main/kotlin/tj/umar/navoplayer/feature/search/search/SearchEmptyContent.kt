package tj.umar.navoplayer.feature.search.search

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.medallion.KhotamHaloMedallion
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.feature.search.R

private const val EMPTY_CROSSFADE_MILLIS = 220
private const val SHAKE_START_DEGREES = -12f
private const val SHAKE_DAMPING = 0.35f
private val MedallionSize = 104.dp
private val HaloSlotSize = MedallionSize * 1.6f

@Composable
internal fun SearchEmptyContent(
    phase: SearchPhase,
    resultsQuery: String,
    modifier: Modifier = Modifier,
) {
    val motion = rememberMotionEnabled()
    Crossfade(
        targetState = phase == SearchPhase.NoResults,
        animationSpec = if (motion) tween(EMPTY_CROSSFADE_MILLIS) else snap(),
        modifier = modifier,
        label = "searchEmpty",
    ) { noResults ->
        if (noResults) {
            SearchNoResults(resultsQuery = resultsQuery, motion = motion)
        } else {
            SearchIdle()
        }
    }
}

@Composable
private fun SearchIdle() {
    StateMessage(
        message = stringResource(R.string.search_idle_hint),
        palette = MedallionPalettes.all[2],
        illustration = { KhotamHaloMedallion(palette = MedallionPalettes.all[2]) },
    )
}

@Composable
private fun SearchNoResults(resultsQuery: String, motion: Boolean) {
    val shake = remember { Animatable(if (motion) SHAKE_START_DEGREES else 0f) }
    LaunchedEffect(Unit) {
        if (motion) {
            shake.animateTo(0f, spring(dampingRatio = SHAKE_DAMPING, stiffness = Spring.StiffnessLow))
        }
    }
    val palette = MedallionPalettes.all[5]
    StateMessage(
        message = stringResource(R.string.search_no_results, resultsQuery),
        palette = palette,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        illustration = {
            Box(modifier = Modifier.size(HaloSlotSize), contentAlignment = Alignment.Center) {
                Medallion(
                    palette = palette,
                    modifier = Modifier.size(MedallionSize),
                    rotationDegrees = { shake.value },
                )
            }
        },
    )
}
