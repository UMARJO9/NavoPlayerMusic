package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
data class NavoTypography(
    val wordmark: TextStyle,
    val wordmarkSmall: TextStyle,
    val displayL: TextStyle,
    val displayM: TextStyle,
    val titleS: TextStyle,
    val body: TextStyle,
    val itemTitle: TextStyle,
    val label: TextStyle,
    val labelStrong: TextStyle,
    val secondary: TextStyle,
    val secondaryNumeric: TextStyle,
    val caption: TextStyle,
    val captionNumeric: TextStyle,
)

private const val TABULAR_NUMBERS = "tnum"

private val SecondaryStyle = TextStyle(fontFamily = GolosText, fontWeight = FontWeight.Normal, fontSize = 14.sp)
private val CaptionStyle = TextStyle(fontFamily = GolosText, fontWeight = FontWeight.Normal, fontSize = 13.sp)

internal val DefaultNavoTypography = NavoTypography(
    wordmark = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        letterSpacing = (-0.01).em,
    ),
    wordmarkSmall = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.01).em,
    ),
    displayL = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 1.15.em,
        letterSpacing = (-0.015).em,
    ),
    displayM = TextStyle(
        fontFamily = Unbounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 1.2.em,
        letterSpacing = (-0.01).em,
    ),
    titleS = TextStyle(fontFamily = GolosText, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    body = TextStyle(fontFamily = GolosText, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 1.5.em),
    itemTitle = TextStyle(fontFamily = GolosText, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    label = TextStyle(fontFamily = GolosText, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    labelStrong = TextStyle(fontFamily = GolosText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    secondary = SecondaryStyle,
    secondaryNumeric = SecondaryStyle.copy(fontFeatureSettings = TABULAR_NUMBERS),
    caption = CaptionStyle,
    captionNumeric = CaptionStyle.copy(fontFeatureSettings = TABULAR_NUMBERS),
)

val LocalNavoTypography = staticCompositionLocalOf { DefaultNavoTypography }

private val BaseMaterialTypography = Typography()

internal val NavoMaterialTypography = Typography(
    displayLarge = BaseMaterialTypography.displayLarge.copy(fontFamily = Unbounded),
    displayMedium = BaseMaterialTypography.displayMedium.copy(fontFamily = Unbounded),
    displaySmall = BaseMaterialTypography.displaySmall.copy(fontFamily = Unbounded),
    headlineLarge = BaseMaterialTypography.headlineLarge.copy(fontFamily = Unbounded),
    headlineMedium = BaseMaterialTypography.headlineMedium.copy(fontFamily = Unbounded),
    headlineSmall = BaseMaterialTypography.headlineSmall.copy(fontFamily = Unbounded),
    titleLarge = BaseMaterialTypography.titleLarge.copy(fontFamily = GolosText),
    titleMedium = BaseMaterialTypography.titleMedium.copy(fontFamily = GolosText),
    titleSmall = BaseMaterialTypography.titleSmall.copy(fontFamily = GolosText),
    bodyLarge = BaseMaterialTypography.bodyLarge.copy(fontFamily = GolosText),
    bodyMedium = BaseMaterialTypography.bodyMedium.copy(fontFamily = GolosText),
    bodySmall = BaseMaterialTypography.bodySmall.copy(fontFamily = GolosText),
    labelLarge = BaseMaterialTypography.labelLarge.copy(fontFamily = GolosText),
    labelMedium = BaseMaterialTypography.labelMedium.copy(fontFamily = GolosText),
    labelSmall = BaseMaterialTypography.labelSmall.copy(fontFamily = GolosText),
)
