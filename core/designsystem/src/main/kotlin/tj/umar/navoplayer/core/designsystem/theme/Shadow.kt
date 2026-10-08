package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

object NavoShadows {
    val MiniPlayer: Shadow = Shadow(
        radius = 32.dp,
        color = ShadowInk,
        offset = DpOffset(0.dp, 12.dp),
        alpha = 0.5f,
    )
    val Medallion: Shadow = Shadow(
        radius = 64.dp,
        color = ShadowInk,
        offset = DpOffset(0.dp, 28.dp),
        alpha = 0.6f,
    )
}
