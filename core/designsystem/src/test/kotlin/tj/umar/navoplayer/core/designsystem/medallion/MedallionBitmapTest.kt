package tj.umar.navoplayer.core.designsystem.medallion

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MedallionBitmapTest {

    private val palette = MedallionPalettes.all.first()

    @Test
    fun `bitmap has requested square size`() {
        val bitmap = palette.renderBitmap(128)

        assertEquals(128, bitmap.width)
        assertEquals(128, bitmap.height)
        assertEquals(Bitmap.Config.ARGB_8888, bitmap.config)
    }

    @Test
    fun `size is clamped to allowed range`() {
        assertEquals(MAX_MEDALLION_BITMAP_PX, palette.renderBitmap(4_000).width)
        assertEquals(1, palette.renderBitmap(0).width)
    }
}
