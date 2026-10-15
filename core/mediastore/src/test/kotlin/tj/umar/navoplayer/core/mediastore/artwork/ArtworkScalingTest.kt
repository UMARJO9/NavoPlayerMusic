package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArtworkScalingTest {

    @Test
    fun `larger bitmap is scaled so short side matches`() {
        val scaled = Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888).cappedToShortSide(512)

        assertEquals(683, scaled.width)
        assertEquals(512, scaled.height)
    }

    @Test
    fun `bitmap within limit is returned as is`() {
        val bitmap = Bitmap.createBitmap(256, 300, Bitmap.Config.ARGB_8888)

        assertSame(bitmap, bitmap.cappedToShortSide(256))
    }
}
