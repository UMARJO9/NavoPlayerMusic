package tj.umar.navoplayer.feature.widget

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.ui.artwork.ArtworkLoader

private class StubArtworkLoader(private val bitmap: Bitmap?) : ArtworkLoader {
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
class WidgetArtworkTest {

    @Test
    fun `cover is cropped to a circle of requested size`() = runTest {
        val cover = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
        val loader = StubArtworkLoader(cover)

        val result = widgetArtwork(loader, ArtworkKey(1, 10), 100)

        assertEquals(100, result.width)
        assertEquals(100, result.height)
        assertEquals(Color.RED, result.getPixel(50, 50))
        assertEquals(0, Color.alpha(result.getPixel(0, 0)))
        assertEquals(listOf(ArtworkKey(1, 10) to 100), loader.requests)
    }

    @Test
    fun `missing cover falls back to medallion`() = runTest {
        val result = widgetArtwork(StubArtworkLoader(null), ArtworkKey(1, null), 100)

        assertEquals(100, result.width)
        assertEquals(0, Color.alpha(result.getPixel(0, 0)))
    }

    @Test
    fun `circle crop keeps source untouched`() {
        val cover = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }

        val cropped = cover.circleCropped(20)

        assertEquals(40, cover.width)
        assertEquals(Color.BLUE, cover.getPixel(0, 0))
        assertEquals(Color.BLUE, cropped.getPixel(10, 10))
    }

    @Test
    fun `disabled cover art skips the loader`() = runTest {
        val loader = StubArtworkLoader(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

        val result = widgetArtwork(loader.enabledWhen(false), ArtworkKey(1, 10), 100)

        assertEquals(100, result.width)
        assertEquals(emptyList<Pair<ArtworkKey, Int>>(), loader.requests)
    }
}
