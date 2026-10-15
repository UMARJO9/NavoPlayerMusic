package tj.umar.navoplayer.feature.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.glance.unit.ColorProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.ThemeMode
import kotlin.math.max
import kotlin.math.min
import androidx.glance.color.ColorProvider as DayNightColorProvider

class NavoWidgetColorsTest {

    @Test
    fun `light and dark modes ignore system state`() {
        for (supportsDayNight in listOf(true, false)) {
            for (systemInDarkTheme in listOf(true, false)) {
                assertEquals(
                    NavoWidgetAppearance.Light,
                    ThemeMode.Light.widgetAppearance(supportsDayNight, systemInDarkTheme),
                )
                assertEquals(
                    NavoWidgetAppearance.Dark,
                    ThemeMode.Dark.widgetAppearance(supportsDayNight, systemInDarkTheme),
                )
            }
        }
    }

    @Test
    fun `system mode uses day night when supported`() {
        assertEquals(NavoWidgetAppearance.DayNight, ThemeMode.System.widgetAppearance(supportsDayNight = true, systemInDarkTheme = true))
        assertEquals(NavoWidgetAppearance.DayNight, ThemeMode.System.widgetAppearance(supportsDayNight = true, systemInDarkTheme = false))
    }

    @Test
    fun `system mode follows night flag without day night support`() {
        assertEquals(NavoWidgetAppearance.Dark, ThemeMode.System.widgetAppearance(supportsDayNight = false, systemInDarkTheme = true))
        assertEquals(NavoWidgetAppearance.Light, ThemeMode.System.widgetAppearance(supportsDayNight = false, systemInDarkTheme = false))
    }

    @Test
    fun `dark colors keep current widget look`() {
        assertEquals(
            NavoWidgetColors(
                content = ColorProvider(Color(0xFFF3EEDF)),
                contentSecondary = ColorProvider(Color(0xFFA9B4D6)),
                onAccent = ColorProvider(Color(0xFF13204A)),
                disabled = ColorProvider(Color(0x66A9B4D6)),
                background = R.drawable.navo_widget_background_dark,
                playBackground = R.drawable.navo_widget_play_background_dark,
            ),
            NavoWidgetAppearance.Dark.widgetColors(),
        )
        assertEquals(Color(0xFF13204A), NavoWidgetTone.Dark.background)
        assertEquals(Color(0xFFE3B04B), NavoWidgetTone.Dark.accent)
    }

    @Test
    fun `light colors use parchment tone and light drawables`() {
        assertEquals(
            NavoWidgetColors(
                content = ColorProvider(Color(0xFF13204A)),
                contentSecondary = ColorProvider(Color(0xFF4A5582)),
                onAccent = ColorProvider(Color(0xFFFFFBF2)),
                disabled = ColorProvider(Color(0x664A5582)),
                background = R.drawable.navo_widget_background_light,
                playBackground = R.drawable.navo_widget_play_background_light,
            ),
            NavoWidgetAppearance.Light.widgetColors(),
        )
    }

    @Test
    fun `day night colors pair light and dark tones with resource drawables`() {
        val light = NavoWidgetTone.Light
        val dark = NavoWidgetTone.Dark
        assertEquals(
            NavoWidgetColors(
                content = DayNightColorProvider(day = light.content, night = dark.content),
                contentSecondary = DayNightColorProvider(day = light.contentSecondary, night = dark.contentSecondary),
                onAccent = DayNightColorProvider(day = light.onAccent, night = dark.onAccent),
                disabled = DayNightColorProvider(day = light.disabled, night = dark.disabled),
                background = R.drawable.navo_widget_background,
                playBackground = R.drawable.navo_widget_play_background,
            ),
            NavoWidgetAppearance.DayNight.widgetColors(),
        )
    }

    @Test
    fun `tones meet contrast requirements`() {
        for (tone in listOf(NavoWidgetTone.Dark, NavoWidgetTone.Light)) {
            assertTrue("$tone content", contrast(tone.content, tone.background) >= 4.5)
            assertTrue("$tone secondary", contrast(tone.contentSecondary, tone.background) >= 4.5)
            assertTrue("$tone on accent", contrast(tone.onAccent, tone.accent) >= 4.5)
            assertTrue("$tone accent", contrast(tone.accent, tone.background) >= 3.0)
        }
    }

    private fun contrast(first: Color, second: Color): Float {
        val a = first.luminance()
        val b = second.luminance()
        return (max(a, b) + 0.05f) / (min(a, b) + 0.05f)
    }
}
