package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Immutable
data class NavoShadows(
    val miniPlayer: Shadow,
    val medallion: Shadow,
)

internal val DarkNavoShadows = NavoShadows(
    miniPlayer = Shadow(
        radius = 32.dp,
        color = ShadowInk,
        offset = DpOffset(0.dp, 12.dp),
        alpha = 0.5f,
    ),
    medallion = Shadow(
        radius = 64.dp,
        color = ShadowInk,
        offset = DpOffset(0.dp, 28.dp),
        alpha = 0.6f,
    ),
)

internal val LightNavoShadows = NavoShadows(
    miniPlayer = Shadow(
        radius = 24.dp,
        color = UmberShadow,
        offset = DpOffset(0.dp, 8.dp),
        alpha = 0.18f,
    ),
    medallion = Shadow(
        radius = 48.dp,
        color = UmberShadow,
        offset = DpOffset(0.dp, 20.dp),
        alpha = 0.28f,
    ),
)

val LocalNavoShadows = staticCompositionLocalOf { DarkNavoShadows }
