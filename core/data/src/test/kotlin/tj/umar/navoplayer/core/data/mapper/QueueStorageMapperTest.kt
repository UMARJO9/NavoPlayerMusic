package tj.umar.navoplayer.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.database.entity.QueueStateEntity
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.testing.data.TestSavedQueues

class QueueStorageMapperTest {

    @Test
    fun `queue round trips without shuffle`() {
        val (state, items) = TestSavedQueues.alphaBeta.toEntities(savedAt = 7)

        assertEquals(TestSavedQueues.alphaBeta, state.toSavedQueue(items))
    }

    @Test
    fun `queue round trips with shuffle order`() {
        val shuffled = TestSavedQueues.alphaBeta.copy(
            shuffleOrder = listOf(1, 0),
            progress = TestSavedQueues.alphaBeta.progress.copy(shuffleEnabled = true),
        )
        val (state, items) = shuffled.toEntities(savedAt = 7)

        assertEquals(listOf(1, 0), items.map { it.shufflePosition })
        assertEquals(shuffled, state.toSavedQueue(items))
    }

    @Test
    fun `broken shuffle positions drop order`() {
        val shuffled = TestSavedQueues.alphaBeta.copy(
            shuffleOrder = listOf(1, 0),
            progress = TestSavedQueues.alphaBeta.progress.copy(shuffleEnabled = true),
        )
        val (state, items) = shuffled.toEntities(savedAt = 7)
        val broken = items.map { it.copy(shufflePosition = 0) }

        assertNull(state.toSavedQueue(broken)?.shuffleOrder)
    }

    @Test
    fun `current index is coerced and empty queue is nothing`() {
        val (state, items) = TestSavedQueues.alphaBeta.toEntities(savedAt = 7)

        assertEquals(1, state.copy(currentIndex = 9).toSavedQueue(items)?.progress?.currentIndex)
        assertNull(state.toSavedQueue(emptyList()))
    }

    @Test
    fun `repeat modes keep storage values`() {
        assertEquals(listOf("off", "all", "one"), RepeatMode.entries.map { it.storageValue() })
        assertEquals(RepeatMode.Off, repeatModeOf("loop"))
    }

    @Test
    fun `every playback source round trips`() {
        val sources = listOf(
            PlaybackSource.AllTracks,
            PlaybackSource.Album("Ватан"),
            PlaybackSource.Album(null),
            PlaybackSource.Artist("Navo"),
            PlaybackSource.Folder(null),
            PlaybackSource.Search("rock"),
            PlaybackSource.Playlist(4, "Mix"),
            PlaybackSource.Favorites,
            PlaybackSource.Queue,
            null,
        )

        sources.forEach { assertEquals(it, it.toStored().toPlaybackSource()) }
        assertNull(StoredPlaybackSource("radio", null, null).toPlaybackSource())
        assertNull(StoredPlaybackSource("playlist", null, "Mix").toPlaybackSource())
    }

    @Test
    fun `stored source columns are read back`() {
        val state = QueueStateEntity(
            currentIndex = 0,
            positionMs = 0,
            shuffleEnabled = false,
            repeatMode = "one",
            sourceKind = "playlist",
            sourcePlaylistId = 3,
            sourceLabel = "Road",
            savedAt = 1,
        )
        val (_, items) = TestSavedQueues.alphaBeta.toEntities(savedAt = 1)

        val saved = state.toSavedQueue(items)!!
        assertEquals(PlaybackSource.Playlist(3, "Road"), saved.source)
        assertEquals(RepeatMode.One, saved.progress.repeatMode)
    }
}
