package tj.umar.navoplayer.artwork

import android.graphics.Bitmap
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tj.umar.navoplayer.core.designsystem.medallion.MAX_MEDALLION_BITMAP_PX
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.mediastore.artwork.AudioArtworkSource

private class StubArtworkSource(private val bitmap: Bitmap?) : AudioArtworkSource {
    val requests = mutableListOf<Pair<ArtworkKey, Int>>()
    override fun cached(key: ArtworkKey, sizePx: Int): Bitmap? = null
    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap? {
        requests += key to sizePx
        return bitmap
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CoverOrMedallionPlaybackArtworkTest {

    @Test
    fun `cover from source is used when present`() = runTest {
        val cover = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val source = StubArtworkSource(cover)

        val result = CoverOrMedallionPlaybackArtwork(source).load(ArtworkKey(1, 10), 512)

        assertSame(cover, result)
        assertEquals(listOf(ArtworkKey(1, 10) to 512), source.requests)
    }

    @Test
    fun `missing cover falls back to opaque medallion`() = runTest {
        val result = CoverOrMedallionPlaybackArtwork(StubArtworkSource(null)).load(ArtworkKey(3, null), 512)

        assertEquals(MAX_MEDALLION_BITMAP_PX, result.width)
        assertEquals(MAX_MEDALLION_BITMAP_PX, result.height)
        assertEquals(MedallionPalettes.forKey(3).background.toArgb(), result.getPixel(0, 0))
    }

    @Test
    fun `small requests render small medallion`() = runTest {
        val result = CoverOrMedallionPlaybackArtwork(StubArtworkSource(null)).load(ArtworkKey(3, null), 64)

        assertEquals(64, result.width)
    }
}
