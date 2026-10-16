package tj.umar.navoplayer.core.designsystem.glass

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Stable
class NavoGlassBackdrop internal constructor(
    internal val layer: LayerBackdrop,
    internal val tier: GlassTier,
)

@Composable
fun rememberNavoGlassBackdrop(): NavoGlassBackdrop {
    val inspection = LocalInspectionMode.current
    val layer = rememberLayerBackdrop()
    return remember(layer, inspection) {
        NavoGlassBackdrop(layer, glassTier(Build.VERSION.SDK_INT, inspection))
    }
}

fun Modifier.navoGlassSource(backdrop: NavoGlassBackdrop?): Modifier =
    if (backdrop == null || backdrop.tier == GlassTier.Tint) this else layerBackdrop(backdrop.layer)

val LocalNavoGlassBackdrop = staticCompositionLocalOf<NavoGlassBackdrop?> { null }
