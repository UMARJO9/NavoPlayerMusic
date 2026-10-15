package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import android.provider.MediaStore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LegacyArtworkDecoderTest {

    private val decoder = LegacyArtworkDecoder(RuntimeEnvironment.getApplication())

    @After
    fun tearDown() {
        AlbumArtFileProvider.files = emptyMap()
        AlbumArtFileProvider.denied = false
    }

    @Test
    fun `missing album art decodes to null`() = runTest {
        Robolectric.setupContentProvider(AlbumArtFileProvider::class.java, MediaStore.AUTHORITY)

        assertNull(decoder.album(albumId = 1, targetPx = 256))
    }

    @Test
    fun `denied album art decodes to null`() = runTest {
        Robolectric.setupContentProvider(AlbumArtFileProvider::class.java, MediaStore.AUTHORITY)
        AlbumArtFileProvider.denied = true

        assertNull(decoder.album(albumId = 5, targetPx = 256))
    }

    @Test
    fun `undecodable album art decodes to null`() = runTest {
        Robolectric.setupContentProvider(AlbumArtFileProvider::class.java, MediaStore.AUTHORITY)
        AlbumArtFileProvider.files = mapOf(2L to byteArrayOf(1, 2, 3, 4))

        assertNull(decoder.album(albumId = 2, targetPx = 256))
    }

    @Test
    fun `album art is downsampled towards target size`() = runTest {
        Robolectric.setupContentProvider(AlbumArtFileProvider::class.java, MediaStore.AUTHORITY)
        AlbumArtFileProvider.files = mapOf(4L to png(width = 600, height = 600))

        val bitmap = decoder.album(albumId = 4, targetPx = 256)

        assertEquals(300, bitmap?.width)
    }

    @Test
    fun `track without readable source decodes to null`() = runTest {
        assertNull(decoder.track(trackId = 3, targetPx = 256))
    }

    private fun png(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}
