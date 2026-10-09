package tj.umar.navoplayer.core.designsystem.medallion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.toArgb
import tj.umar.navoplayer.core.designsystem.theme.Ivory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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

    @Test
    fun `png corners are filled with palette background`() {
        val png = palette.renderPng(64)

        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)

        assertEquals(palette.background.toArgb(), decoded.getPixel(0, 0))
    }

    @Test
    fun `plain bitmap keeps transparent corners`() {
        assertEquals(0, palette.renderBitmap(64).getPixel(0, 0))
    }

    @Test
    fun `detailed suzani medallion renders ivory center inside transparent corners`() {
        val bitmap = palette.renderBitmap(128, MedallionVariant.Detailed)

        assertEquals(0, bitmap.getPixel(0, 0))
        assertEquals(Ivory.toArgb(), bitmap.getPixel(64, 64))
    }

    @Test
    fun `simple khotam medallion has star point on top and visible center dot for every palette`() {
        MedallionPalettes.all.forEach { each ->
            val bitmap = each.renderBitmap(100)

            assertEquals(each.petals.toArgb(), bitmap.getPixel(50, 7))
            assertNotEquals(bitmap.getPixel(55, 36), bitmap.getPixel(50, 50))
        }
    }
}
