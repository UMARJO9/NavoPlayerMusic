package tj.umar.navoplayer.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSortField

class SortStorageMapperTest {

    @Test
    fun `track sort fields keep stable storage values`() {
        assertEquals(
            listOf("title", "artist", "album", "date_added", "duration"),
            TrackSortField.entries.map { it.storageValue() },
        )
    }

    @Test
    fun `group sort fields keep stable storage values`() {
        assertEquals(listOf("name", "track_count"), GroupSortField.entries.map { it.storageValue() })
    }

    @Test
    fun `stored values map back to fields`() {
        TrackSortField.entries.forEach { assertEquals(it, trackSortFieldOf(it.storageValue())) }
        GroupSortField.entries.forEach { assertEquals(it, groupSortFieldOf(it.storageValue())) }
    }

    @Test
    fun `missing or unknown values fall back to defaults`() {
        assertEquals(TrackSortField.Title, trackSortFieldOf(null))
        assertEquals(TrackSortField.Title, trackSortFieldOf("rating"))
        assertEquals(GroupSortField.Name, groupSortFieldOf(null))
        assertEquals(GroupSortField.Name, groupSortFieldOf("size"))
    }

    @Test
    fun `descending flag maps to direction`() {
        assertEquals(SortDirection.Descending, directionOf(true))
        assertEquals(SortDirection.Ascending, directionOf(false))
    }
}
