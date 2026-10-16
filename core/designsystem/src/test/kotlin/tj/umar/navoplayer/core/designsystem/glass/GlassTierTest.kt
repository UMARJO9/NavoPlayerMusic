package tj.umar.navoplayer.core.designsystem.glass

import org.junit.Assert.assertEquals
import org.junit.Test

class GlassTierTest {

    @Test
    fun `inspection always uses tint`() {
        assertEquals(GlassTier.Tint, glassTier(sdkInt = 36, inspection = true))
    }

    @Test
    fun `below api 31 uses tint`() {
        assertEquals(GlassTier.Tint, glassTier(sdkInt = 24, inspection = false))
        assertEquals(GlassTier.Tint, glassTier(sdkInt = 30, inspection = false))
    }

    @Test
    fun `api 31 and 32 use blur`() {
        assertEquals(GlassTier.Blur, glassTier(sdkInt = 31, inspection = false))
        assertEquals(GlassTier.Blur, glassTier(sdkInt = 32, inspection = false))
    }

    @Test
    fun `api 33 and above use refraction`() {
        assertEquals(GlassTier.Refraction, glassTier(sdkInt = 33, inspection = false))
        assertEquals(GlassTier.Refraction, glassTier(sdkInt = 36, inspection = false))
    }
}
