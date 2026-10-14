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
internal val Crimson = Color(0xFFC8414B)
internal val Turquoise = Color(0xFF2E7D74)

internal val Parchment = Color(0xFFF6F0E1)
internal val Sand = Color(0xFFECE3CC)
internal val Dune = Color(0xFFE4D8BB)
internal val ParchmentLine = Color(0xFFB9A982)
internal val LapisMist = Color(0xFF4A5582)
internal val LapisMuted = Color(0xFF3A4670)
internal val LapisOnDune = Color(0xFF434E7A)
internal val BukharaGold = Color(0xFF7F5208)
internal val IvoryLight = Color(0xFFFFFBF2)
internal val CrimsonDeep = Color(0xFFA8323C)
internal val TurquoiseDeep = Color(0xFF1F6B63)
internal val UmberShadow = Color(0xFF5C4520)
internal val ParchmentOrnament = Color(0xFFC8973A)

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
    val isDark: Boolean,
    val card: Color,
    val divider: Color,
    val ornament: Color,
    val highlight: Color,
    val crimson: Color,
    val turquoise: Color,
    val onTile: Color,
    val medallionRim: Color,
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
    isDark = true,
    card = LapisRaised,
    divider = Color(0x1FF3EEDF),
    ornament = Accent,
    highlight = Ivory,
    crimson = Crimson,
    turquoise = Turquoise,
    onTile = IvoryLight,
    medallionRim = Color.Transparent,
)

internal val ParchmentNavoColors = NavoColors(
    background = Parchment,
    raised = Sand,
    high = Dune,
    line = ParchmentLine,
    content = Lapis,
    contentSecondary = LapisMist,
    contentMuted = LapisMuted,
    contentOnHigh = LapisOnDune,
    accent = BukharaGold,
    onAccent = IvoryLight,
    shadow = UmberShadow,
    isDark = false,
    card = IvoryLight,
    divider = Color(0x1F13204A),
    ornament = ParchmentOrnament,
    highlight = Color.White,
    crimson = CrimsonDeep,
    turquoise = TurquoiseDeep,
    onTile = IvoryLight,
    medallionRim = Color(0x2913204A),
)

val LocalNavoColors = staticCompositionLocalOf { LapisNavoColors }
