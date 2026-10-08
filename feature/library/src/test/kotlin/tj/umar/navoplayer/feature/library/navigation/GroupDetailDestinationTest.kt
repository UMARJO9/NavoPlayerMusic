package tj.umar.navoplayer.feature.library.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType

class GroupDetailDestinationTest {

    @Test
    fun `keys survive round trip through destination`() {
        val keys = listOf(
            TrackGroupKey(TrackGroupType.Album, 10, null),
            TrackGroupKey(TrackGroupType.Album, null, "Ёлка"),
            TrackGroupKey(TrackGroupType.Artist, 7, null),
            TrackGroupKey(TrackGroupType.Artist, null, null),
            TrackGroupKey(TrackGroupType.Folder, null, "Music/Navo"),
            TrackGroupKey(TrackGroupType.Folder, null, null),
        )

        keys.forEach { key -> assertEquals(key, key.toDestination().toKey()) }
    }
}
