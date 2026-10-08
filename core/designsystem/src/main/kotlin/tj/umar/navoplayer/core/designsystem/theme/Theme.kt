package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val NavoDarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Lapis,
    primaryContainer = LapisRaised,
    onPrimaryContainer = Ivory,
    secondary = Mist,
    onSecondary = Lapis,
    background = Lapis,
    onBackground = Ivory,
    surface = Lapis,
    onSurface = Ivory,
    surfaceVariant = LapisRaised,
    onSurfaceVariant = Mist,
    surfaceContainer = LapisRaised,
    surfaceContainerHigh = LapisHigh,
    outline = LapisLine,
    outlineVariant = LapisLine,
)

@Composable
fun NavoTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalNavoColors provides LapisNavoColors,
        LocalNavoTypography provides DefaultNavoTypography,
    ) {
        MaterialTheme(
            colorScheme = NavoDarkColorScheme,
            typography = NavoMaterialTypography,
            content = content,
        )
    }
}

object NavoTheme {
    val colors: NavoColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNavoColors.current

    val typography: NavoTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalNavoTypography.current
}
