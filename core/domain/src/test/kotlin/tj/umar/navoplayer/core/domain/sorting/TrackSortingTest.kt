package tj.umar.navoplayer.core.domain.sorting

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackSort
import tj.umar.navoplayer.core.domain.model.TrackSortField
import tj.umar.navoplayer.core.testing.data.TestTracks
import java.text.Collator
import java.util.Locale

class TrackSortingTest {

    private val english = Collator.getInstance(Locale.ENGLISH).apply { strength = Collator.PRIMARY }

    private fun track(
        id: Long,
        title: String = "Song",
        artist: String? = "Artist",
        album: String? = "Album",
        date: Long? = null,
        duration: Long = 1_000,
        number: Int? = null,
    ) = TestTracks.alpha.copy(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = null,
        dateAddedMs = date,
        durationMs = duration,
        trackNumber = number,
        discNumber = null,
    )

    private fun List<Track>.ids(
        field: TrackSortField,
        direction: SortDirection = SortDirection.Ascending,
        collator: Collator = english,
    ) = sortedFor(TrackSort(field, direction), collator).map { it.id }

    @Test
    fun `title ignores case and accents`() {
        val tracks = listOf(track(1, "beta"), track(2, "Alpha"), track(3, "Ébène"), track(4, "Echo"))

        assertEquals(listOf(2L, 1L, 3L, 4L), tracks.ids(TrackSortField.Title))
    }

    @Test
    fun `cyrillic titles sort alphabetically`() {
        val tracks = listOf(track(1, "яблоко"), track(2, "Арбуз"), track(3, "банан"))

        assertEquals(listOf(2L, 3L, 1L), tracks.ids(TrackSortField.Title, collator = Collator.getInstance(Locale.forLanguageTag("ru"))))
    }

    @Test
    fun `equal titles fall back to artist then id`() {
        val tracks = listOf(track(3, artist = "B"), track(2, artist = "A"), track(1, artist = "A"))

        assertEquals(listOf(1L, 2L, 3L), tracks.ids(TrackSortField.Title))
    }

    @Test
    fun `blank title stays last in both directions`() {
        val tracks = listOf(track(1, ""), track(2, "A"), track(3, "B"))

        assertEquals(listOf(2L, 3L, 1L), tracks.ids(TrackSortField.Title))
        assertEquals(listOf(3L, 2L, 1L), tracks.ids(TrackSortField.Title, SortDirection.Descending))
    }

    @Test
    fun `artist keeps album order and unknown artist last`() {
        val tracks = listOf(
            track(1, "Z", artist = "Bee", number = 2),
            track(2, "Y", artist = null),
            track(3, "X", artist = "Bee", number = 1),
            track(4, "W", artist = "Ant"),
        )

        assertEquals(listOf(4L, 3L, 1L, 2L), tracks.ids(TrackSortField.Artist))
        assertEquals(listOf(3L, 1L, 4L, 2L), tracks.ids(TrackSortField.Artist, SortDirection.Descending))
    }

    @Test
    fun `album uses track numbers and unknown album last`() {
        val tracks = listOf(track(1, album = "B", number = 2), track(2, album = null), track(3, album = "B", number = 1), track(4, album = "A"))

        assertEquals(listOf(4L, 3L, 1L, 2L), tracks.ids(TrackSortField.Album))
    }

    @Test
    fun `date added sorts both ways with missing last`() {
        val tracks = listOf(track(1, date = 200), track(2, date = null), track(3, date = 100))

        assertEquals(listOf(3L, 1L, 2L), tracks.ids(TrackSortField.DateAdded))
        assertEquals(listOf(1L, 3L, 2L), tracks.ids(TrackSortField.DateAdded, SortDirection.Descending))
    }

    @Test
    fun `duration puts unknown last`() {
        val tracks = listOf(track(1, duration = 300), track(2, duration = 0), track(3, duration = 100))

        assertEquals(listOf(3L, 1L, 2L), tracks.ids(TrackSortField.Duration))
        assertEquals(listOf(1L, 3L, 2L), tracks.ids(TrackSortField.Duration, SortDirection.Descending))
    }

    @Test
    fun `descending flips only the main key`() {
        val tracks = listOf(track(1, "B", duration = 100), track(2, "A", duration = 100), track(3, "C", duration = 200))

        assertEquals(listOf(3L, 2L, 1L), tracks.ids(TrackSortField.Duration, SortDirection.Descending))
    }
}
