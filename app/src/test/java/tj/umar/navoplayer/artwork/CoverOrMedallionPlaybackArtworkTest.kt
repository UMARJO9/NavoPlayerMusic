package tj.umar.navoplayer.artwork

import android.graphics.Bitmap
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tj.umar.navoplayer.core.designsystem.medallion.MAX_MEDALLION_BITMAP_PX
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.ObserveShowCoverArtUseCase
import tj.umar.navoplayer.core.mediastore.artwork.AudioArtworkSource
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

private class StubArtworkSource(private val bitmap: Bitmap?) : AudioArtworkSource {
    val requests = mutableListOf<Pair<ArtworkKey, Int>>()
    override fun cached(key: ArtworkKey, sizePx: Int): Bitmap? = bitmap
    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap? {
        requests += key to sizePx
        return bitmap
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CoverOrMedallionPlaybackArtworkTest {

    private val settings = FakeSettingsRepository()

    private fun TestScope.preference(): CoverArtPreference =
        CoverArtPreference(ObserveShowCoverArtUseCase(settings), backgroundScope).also { runCurrent() }

    @Test
    fun `cover from source is used when present`() = runTest {
        val cover = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val source = StubArtworkSource(cover)

        val result = CoverOrMedallionPlaybackArtwork(source, preference()).load(ArtworkKey(1, 10), 512)

        assertSame(cover, result)
        assertEquals(listOf(ArtworkKey(1, 10) to 512), source.requests)
    }

    @Test
    fun `oversized cover is scaled to requested size`() = runTest {
        val source = StubArtworkSource(Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888))

        val result = CoverOrMedallionPlaybackArtwork(source, preference()).load(ArtworkKey(1, 10), 512)

        assertEquals(512, result.width)
    }

    @Test
    fun `missing cover falls back to opaque medallion`() = runTest {
        val result = CoverOrMedallionPlaybackArtwork(StubArtworkSource(null), preference()).load(ArtworkKey(3, null), 512)

        assertEquals(MAX_MEDALLION_BITMAP_PX, result.width)
        assertEquals(MAX_MEDALLION_BITMAP_PX, result.height)
        assertEquals(MedallionPalettes.forKey(3).background.toArgb(), result.getPixel(0, 0))
    }

    @Test
    fun `small requests render small medallion`() = runTest {
        val result = CoverOrMedallionPlaybackArtwork(StubArtworkSource(null), preference()).load(ArtworkKey(3, null), 64)

        assertEquals(64, result.width)
    }

    @Test
    fun `disabled setting always renders medallion`() = runTest {
        settings.emit(UserSettings(showCoverArt = false))
        val source = StubArtworkSource(Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888))

        val result = CoverOrMedallionPlaybackArtwork(source, preference()).load(ArtworkKey(1, 10), 128)

        assertEquals(128, result.width)
        assertTrue(source.requests.isEmpty())
    }

    @Test
    fun `loader is gated by the setting`() = runTest {
        val cover = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val loader = MediaStoreArtworkLoader(StubArtworkSource(cover), preference())

        assertSame(cover, loader.cached(ArtworkKey(1, 10), 64))
        assertSame(cover, loader.load(ArtworkKey(1, 10), 64))

        settings.setShowCoverArt(false)
        runCurrent()

        assertNull(loader.cached(ArtworkKey(1, 10), 64))
        assertNull(loader.load(ArtworkKey(1, 10), 64))
    }

    @Test
    fun `preference defaults to shown when settings fail`() = runTest {
        settings.observeError = IllegalStateException("disk")

        assertTrue(preference().enabled.value)
    }
}
