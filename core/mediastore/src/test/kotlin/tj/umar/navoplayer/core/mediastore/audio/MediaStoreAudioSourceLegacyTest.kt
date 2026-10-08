package tj.umar.navoplayer.core.mediastore.audio

import android.provider.MediaStore
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MediaStoreAudioSourceLegacyTest {

    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        Robolectric.setupContentProvider(FakeMediaProvider::class.java, MediaStore.AUTHORITY)
    }

    @After
    fun tearDown() {
        FakeMediaProvider.rows = emptyList()
        FakeMediaProvider.lastProjection = null
    }

    @Test
    fun `reads data path on legacy storage`() = runTest {
        FakeMediaProvider.rows = listOf(
            mapOf(
                MediaStore.Audio.Media._ID to 3L,
                MediaStore.Audio.Media.TITLE to "Song",
                "_data" to "/storage/emulated/0/Music/song.mp3",
            ),
        )

        val row = source().queryAudio().single()

        assertEquals("/storage/emulated/0/Music/song.mp3", row.dataPath)
        assertEquals(null, row.relativePath)
        assertTrue(FakeMediaProvider.lastProjection.orEmpty().contains("_data"))
    }

    private fun TestScope.source() = MediaStoreAudioSource(context, UnconfinedTestDispatcher(testScheduler))
}
