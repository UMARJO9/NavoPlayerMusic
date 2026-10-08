package tj.umar.navoplayer.core.domain.sorting

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.grouping.toAlbums
import tj.umar.navoplayer.core.domain.grouping.toFolders
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.testing.data.TestTracks

class GroupSortingTest {

    private val tracks = listOf(
        TestTracks.alpha.copy(id = 1, album = "Beta", albumId = 1, folderPath = "Music/B"),
        TestTracks.alpha.copy(id = 2, album = "Beta", albumId = 1, folderPath = "Music/B"),
        TestTracks.alpha.copy(id = 3, album = "Alpha", albumId = 2, folderPath = "Music/A"),
        TestTracks.alpha.copy(id = 4, album = null, albumId = null, folderPath = null),
    )

    @Test
    fun `name ascending matches default grouping order`() {
        val albums = tracks.toAlbums()

        assertEquals(albums, albums.reversed().sortedFor(GroupSort.Default))
        assertEquals(listOf("Alpha", "Beta", null), albums.map { it.title })
    }

    @Test
    fun `name descending keeps unknown last`() {
        val sorted = tracks.toAlbums().sortedFor(GroupSort(GroupSortField.Name, SortDirection.Descending))

        assertEquals(listOf("Beta", "Alpha", null), sorted.map { it.title })
    }

    @Test
    fun `track count sorts both ways with name ties`() {
        val folders = tracks.toFolders()

        assertEquals(
            listOf("A", "B", null),
            folders.sortedFor(GroupSort(GroupSortField.TrackCount, SortDirection.Ascending)).map { it.name },
        )
        assertEquals(
            listOf("B", "A", null),
            folders.sortedFor(GroupSort(GroupSortField.TrackCount, SortDirection.Descending)).map { it.name },
        )
    }
}
