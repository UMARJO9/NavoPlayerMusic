package tj.umar.navoplayer.feature.widget

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import tj.umar.navoplayer.core.domain.model.ThemeMode
import androidx.glance.color.ColorProvider as DayNightColorProvider

internal data class NavoWidgetTone(
    val background: Color,
    val content: Color,
    val contentSecondary: Color,
    val accent: Color,
    val onAccent: Color,
    val disabled: Color,
) {
    companion object {
        val Dark = NavoWidgetTone(
            background = Color(0xFF13204A),
            content = Color(0xFFF3EEDF),
            contentSecondary = Color(0xFFA9B4D6),
            accent = Color(0xFFE3B04B),
            onAccent = Color(0xFF13204A),
            disabled = Color(0x66A9B4D6),
        )
        val Light = NavoWidgetTone(
            background = Color(0xFFF6F0E1),
            content = Color(0xFF13204A),
            contentSecondary = Color(0xFF4A5582),
            accent = Color(0xFF7F5208),
            onAccent = Color(0xFFFFFBF2),
            disabled = Color(0x664A5582),
        )
    }
}

internal enum class NavoWidgetAppearance { Light, Dark, DayNight }

internal fun ThemeMode.widgetAppearance(supportsDayNight: Boolean, systemInDarkTheme: Boolean): NavoWidgetAppearance =
    when (this) {
        ThemeMode.Light -> NavoWidgetAppearance.Light
        ThemeMode.Dark -> NavoWidgetAppearance.Dark
        ThemeMode.System -> when {
            supportsDayNight -> NavoWidgetAppearance.DayNight
            systemInDarkTheme -> NavoWidgetAppearance.Dark
            else -> NavoWidgetAppearance.Light
        }
    }

internal data class NavoWidgetColors(
    val content: ColorProvider,
    val contentSecondary: ColorProvider,
    val onAccent: ColorProvider,
    val disabled: ColorProvider,
    @param:DrawableRes val background: Int,
    @param:DrawableRes val playBackground: Int,
)

internal fun NavoWidgetAppearance.widgetColors(): NavoWidgetColors = when (this) {
    NavoWidgetAppearance.Light -> NavoWidgetTone.Light.fixedColors(
        background = R.drawable.navo_widget_background_light,
        playBackground = R.drawable.navo_widget_play_background_light,
    )
    NavoWidgetAppearance.Dark -> NavoWidgetTone.Dark.fixedColors(
        background = R.drawable.navo_widget_background_dark,
        playBackground = R.drawable.navo_widget_play_background_dark,
    )
    NavoWidgetAppearance.DayNight -> {
        val day = NavoWidgetTone.Light
        val night = NavoWidgetTone.Dark
        NavoWidgetColors(
            content = DayNightColorProvider(day = day.content, night = night.content),
            contentSecondary = DayNightColorProvider(day = day.contentSecondary, night = night.contentSecondary),
            onAccent = DayNightColorProvider(day = day.onAccent, night = night.onAccent),
            disabled = DayNightColorProvider(day = day.disabled, night = night.disabled),
            background = R.drawable.navo_widget_background,
            playBackground = R.drawable.navo_widget_play_background,
        )
    }
}

private fun NavoWidgetTone.fixedColors(@DrawableRes background: Int, @DrawableRes playBackground: Int) = NavoWidgetColors(
    content = ColorProvider(content),
    contentSecondary = ColorProvider(contentSecondary),
    onAccent = ColorProvider(onAccent),
    disabled = ColorProvider(disabled),
    background = background,
    playBackground = playBackground,
)
