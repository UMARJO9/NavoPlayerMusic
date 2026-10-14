package tj.umar.navoplayer.theme

import android.app.UiModeManager
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.ThemeMode

class ApplicationNightModeTest {

    @Test
    fun `theme modes map to application night modes`() {
        assertEquals(UiModeManager.MODE_NIGHT_AUTO, ThemeMode.System.applicationNightMode())
        assertEquals(UiModeManager.MODE_NIGHT_NO, ThemeMode.Light.applicationNightMode())
        assertEquals(UiModeManager.MODE_NIGHT_YES, ThemeMode.Dark.applicationNightMode())
    }
}
