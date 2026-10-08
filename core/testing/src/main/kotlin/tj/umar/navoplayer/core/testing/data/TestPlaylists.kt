package tj.umar.navoplayer.core.testing.data

import tj.umar.navoplayer.core.domain.model.Playlist

object TestPlaylists {

    const val MISSING_TRACK_ID = 999L

    val morning = Playlist(
        id = 1,
        name = "Утро",
        createdAtMs = 10,
        updatedAtMs = 20,
        trackIds = listOf(TestTracks.longMix.id, MISSING_TRACK_ID, TestTracks.alpha.id),
    )

    val empty = Playlist(
        id = 2,
        name = "Пустой",
        createdAtMs = 11,
        updatedAtMs = 30,
        trackIds = emptyList(),
    )

    val all: List<Playlist> = listOf(morning, empty)
}
