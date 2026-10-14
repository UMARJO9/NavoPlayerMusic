package tj.umar.navoplayer.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.ThemeMode

class ThemeModeStorageMapperTest {

    @Test
    fun `theme modes keep stable storage values`() {
        assertEquals(listOf("system", "light", "dark"), ThemeMode.entries.map { it.storageValue() })
    }

    @Test
    fun `stored values map back to modes`() {
        ThemeMode.entries.forEach { assertEquals(it, themeModeOf(it.storageValue())) }
    }

    @Test
    fun `missing or unknown values fall back to default`() {
        assertEquals(ThemeMode.Default, themeModeOf(null))
        assertEquals(ThemeMode.Default, themeModeOf("sepia"))
    }
}
