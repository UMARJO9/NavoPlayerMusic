package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val Lapis = Color(0xFF13204A)
internal val LapisRaised = Color(0xFF1B2B5E)
internal val LapisHigh = Color(0xFF24387A)
internal val LapisLine = Color(0xFF2A3C78)
internal val Ivory = Color(0xFFF3EEDF)
internal val Mist = Color(0xFFA9B4D6)
internal val MistLight = Color(0xFFC9D1EA)
internal val MistOnHigh = Color(0xFFBAC3E0)
internal val Accent = Color(0xFFE3B04B)
internal val ShadowInk = Color(0xFF060C24)

@Immutable
data class NavoColors(
    val background: Color,
    val raised: Color,
    val high: Color,
    val line: Color,
    val content: Color,
    val contentSecondary: Color,
    val contentMuted: Color,
    val contentOnHigh: Color,
    val accent: Color,
    val onAccent: Color,
    val shadow: Color,
)

internal val LapisNavoColors = NavoColors(
    background = Lapis,
    raised = LapisRaised,
    high = LapisHigh,
    line = LapisLine,
    content = Ivory,
    contentSecondary = Mist,
    contentMuted = MistLight,
    contentOnHigh = MistOnHigh,
    accent = Accent,
    onAccent = Lapis,
    shadow = ShadowInk,
)

val LocalNavoColors = staticCompositionLocalOf { LapisNavoColors }
