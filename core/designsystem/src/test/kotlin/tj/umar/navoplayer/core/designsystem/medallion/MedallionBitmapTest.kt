package tj.umar.navoplayer.core.designsystem.medallion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    @Test
    fun `png decodes to square bitmap of requested size`() {
        val png = palette.renderPng(64)

        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)

        assertEquals(64, decoded.width)
        assertEquals(64, decoded.height)
    }

    @Test
    fun `png defaults to max size`() {
        val png = palette.renderPng()

        assertEquals(MAX_MEDALLION_BITMAP_PX, BitmapFactory.decodeByteArray(png, 0, png.size).width)
    }
}
