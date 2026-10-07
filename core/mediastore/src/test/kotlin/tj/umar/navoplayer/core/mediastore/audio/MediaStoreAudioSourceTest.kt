package tj.umar.navoplayer.core.mediastore.audio

import android.provider.MediaStore
import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MediaStoreAudioSourceTest {

    private val context = RuntimeEnvironment.getApplication()
    private val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    @Before
    fun setUp() {
        Robolectric.setupContentProvider(FakeMediaProvider::class.java, MediaStore.AUTHORITY)
    }

    @After
    fun tearDown() {
        FakeMediaProvider.rows = emptyList()
        FakeMediaProvider.lastSelection = null
    }

    @Test
    fun `reads all columns of a row`() = runTest {
        FakeMediaProvider.rows = listOf(fullRow(id = 5))

        val rows = source().queryAudio()

        val expected = MediaStoreAudioRow(
            id = 5,
            title = "Song",
            displayName = "song.mp3",
            artist = "Artist",
            artistId = 50,
            album = "Album",
            albumId = 500,
            durationMs = 180_000,
            track = 1003,
            contentUri = "$collection/5",
        )
        assertEquals(listOf(expected), rows)
    }

    @Test
    fun `missing values are read as null`() = runTest {
        FakeMediaProvider.rows = listOf(
            fullRow(id = 6) + mapOf(
                MediaStore.Audio.Media.TITLE to null,
                MediaStore.Audio.Media.ARTIST to null,
                MediaStore.Audio.Media.ARTIST_ID to null,
                MediaStore.Audio.Media.ALBUM_ID to null,
                MediaStore.Audio.AudioColumns.DURATION to null,
                MediaStore.Audio.Media.TRACK to null,
            ),
        )

        val row = source().queryAudio().single()

        assertEquals(null, row.title)
        assertEquals(null, row.artist)
        assertEquals(null, row.artistId)
        assertEquals(null, row.albumId)
        assertEquals(null, row.durationMs)
        assertEquals(null, row.track)
    }

    @Test
    fun `queries only music`() = runTest {
        source().queryAudio()

        assertEquals("${MediaStore.Audio.Media.IS_MUSIC} != 0", FakeMediaProvider.lastSelection)
    }

    @Test
    fun `empty provider returns empty list`() = runTest {
        assertEquals(emptyList<MediaStoreAudioRow>(), source().queryAudio())
    }

    @Test
    fun `emits when media changes`() = runTest {
        source().observeChanges().test {
            context.contentResolver.notifyChange(collection, null)
            awaitItem()
        }
    }

    private fun TestScope.source() =
        MediaStoreAudioSource(context, UnconfinedTestDispatcher(testScheduler))

    private fun fullRow(id: Long): Map<String, Any?> = mapOf(
        MediaStore.Audio.Media._ID to id,
        MediaStore.Audio.Media.TITLE to "Song",
        MediaStore.Audio.Media.DISPLAY_NAME to "song.mp3",
        MediaStore.Audio.Media.ARTIST to "Artist",
        MediaStore.Audio.Media.ARTIST_ID to 50L,
        MediaStore.Audio.Media.ALBUM to "Album",
        MediaStore.Audio.Media.ALBUM_ID to 500L,
        MediaStore.Audio.AudioColumns.DURATION to 180_000L,
        MediaStore.Audio.Media.TRACK to 1003,
    )
}
