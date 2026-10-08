package tj.umar.navoplayer.core.domain.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.LibraryFolder
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.testing.data.TestTracks

class LibraryFilterTest {

    private val song = TestTracks.alpha.copy(id = 1, durationMs = 180_000, folderPath = "Music/Navo")
    private val clip = TestTracks.alpha.copy(id = 2, durationMs = 8_000, folderPath = "Recordings")
    private val exact = TestTracks.alpha.copy(id = 3, durationMs = 30_000, folderPath = "Recordings/Voice")
    private val unknown = TestTracks.alpha.copy(id = 4, durationMs = 0, folderPath = null)
    private val tracks = listOf(song, clip, exact, unknown)

    private fun filter(duration: MinTrackDuration = MinTrackDuration.Off, excluded: Set<String> = emptySet()) =
        UserSettings(minTrackDuration = duration, excludedFolders = excluded).toLibraryFilter()

    @Test
    fun `no op filter returns same list`() {
        assertSame(tracks, tracks.filteredBy(filter()))
    }

    @Test
    fun `short tracks are hidden but threshold and unknown are kept`() {
        val result = tracks.filteredBy(filter(MinTrackDuration.ThirtySeconds))

        assertEquals(listOf(song, exact, unknown), result)
    }

    @Test
    fun `excluded folder hides only exact path`() {
        val result = tracks.filteredBy(filter(excluded = setOf("Recordings")))

        assertEquals(listOf(song, exact, unknown), result)
    }

    @Test
    fun `duration and folder rules combine`() {
        val result = tracks.filteredBy(filter(MinTrackDuration.SixtySeconds, setOf("Music/Navo")))

        assertEquals(listOf(unknown), result)
    }

    @Test
    fun `folders are counted sorted and flagged`() {
        val folders = (tracks + clip.copy(id = 5)).toLibraryFolders(excluded = setOf("Recordings"))

        assertEquals(
            listOf(
                LibraryFolder("Music/Navo", "Navo", 1, isExcluded = false),
                LibraryFolder("Recordings", "Recordings", 2, isExcluded = true),
                LibraryFolder("Recordings/Voice", "Voice", 1, isExcluded = false),
            ),
            folders,
        )
    }

    @Test
    fun `unknown seconds map to off`() {
        assertEquals(MinTrackDuration.ThirtySeconds, MinTrackDuration.fromSeconds(30))
        assertEquals(MinTrackDuration.Off, MinTrackDuration.fromSeconds(42))
    }

    @Test
    fun `excluded folder without tracks stays listed`() {
        val folders = listOf(song).toLibraryFolders(excluded = setOf("Old/Gone"))

        assertEquals(LibraryFolder("Old/Gone", "Gone", 0, isExcluded = true), folders.first())
    }
}
