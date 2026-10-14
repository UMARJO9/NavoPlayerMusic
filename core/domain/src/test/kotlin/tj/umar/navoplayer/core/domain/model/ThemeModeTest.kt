package tj.umar.navoplayer.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `default is dark`() {
        assertEquals(ThemeMode.Dark, ThemeMode.Default)
        assertEquals(ThemeMode.Dark, UserSettings().themeMode)
    }

    @Test
    fun `system follows the system setting`() {
        assertTrue(ThemeMode.System.resolveDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.System.resolveDarkTheme(systemInDarkTheme = false))
    }

    @Test
    fun `explicit modes ignore the system setting`() {
        listOf(true, false).forEach { system ->
            assertFalse(ThemeMode.Light.resolveDarkTheme(system))
            assertTrue(ThemeMode.Dark.resolveDarkTheme(system))
        }
    }
}
