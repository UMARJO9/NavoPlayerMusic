package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

private val NavoLightColorScheme = lightColorScheme(
    primary = BukharaGold,
    onPrimary = IvoryLight,
    primaryContainer = Sand,
    onPrimaryContainer = Lapis,
    secondary = LapisMist,
    onSecondary = IvoryLight,
    background = Parchment,
    onBackground = Lapis,
    surface = Parchment,
    onSurface = Lapis,
    surfaceVariant = Sand,
    onSurfaceVariant = LapisMist,
    surfaceContainer = Sand,
    surfaceContainerHigh = Dune,
    surfaceContainerLow = IvoryLight,
    outline = ParchmentLine,
    outlineVariant = ParchmentLine,
)

@Composable
fun NavoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalNavoColors provides if (darkTheme) LapisNavoColors else ParchmentNavoColors,
        LocalNavoTypography provides DefaultNavoTypography,
        LocalNavoShadows provides if (darkTheme) DarkNavoShadows else LightNavoShadows,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) NavoDarkColorScheme else NavoLightColorScheme,
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

    val shadows: NavoShadows
        @Composable
        @ReadOnlyComposable
        get() = LocalNavoShadows.current
}
