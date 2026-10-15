package tj.umar.navoplayer.core.mediastore.artwork

import android.provider.MediaStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThumbnailArtworkDecoderTest {

    private val decoder = ThumbnailArtworkDecoder(RuntimeEnvironment.getApplication())

    @Before
    fun setUp() {
        AlbumArtFileProvider.files = emptyMap()
        Robolectric.setupContentProvider(AlbumArtFileProvider::class.java, MediaStore.AUTHORITY)
    }

    @Test
    fun `missing album thumbnail is null`() = runTest {
        assertNull(decoder.album(albumId = 1, targetPx = 256))
    }

    @Test
    fun `missing track thumbnail is null`() = runTest {
        assertNull(decoder.track(trackId = 1, targetPx = 256))
    }
}
