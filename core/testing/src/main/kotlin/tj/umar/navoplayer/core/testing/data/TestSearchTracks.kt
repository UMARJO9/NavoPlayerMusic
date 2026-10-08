package tj.umar.navoplayer.core.testing.data

import tj.umar.navoplayer.core.domain.model.Track

object TestSearchTracks {

    private fun track(
        id: Long,
        title: String,
        artist: String? = null,
        album: String? = null,
        albumId: Long? = null,
        artistId: Long? = null,
    ) = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        artistId = artistId,
        durationMs = 200_000,
        trackNumber = null,
        contentUri = "content://media/external/audio/media/$id",
        folderPath = "Music",
        discNumber = null,
        albumArtist = null,
    )

    val javoni = track(1, "Ҷавонӣ", artist = "Daler Nazarov", album = "Ватан", albumId = 100, artistId = 10)
    val vatan = track(2, "Ватан", artist = "Daler Nazarov", album = "Ватан", albumId = 100, artistId = 10)
    val yolka = track(3, "Ёлка", artist = "Елена", artistId = 11)
    val moi = track(4, "Мой край")
    val moiTwo = track(5, "Мои песни")
    val dejaVu = track(6, "Déjà Vu", artist = "Beyoncé", artistId = 12)
    val backInBlack = track(7, "Back in Black", artist = "AC/DC", album = "Back in Black", albumId = 101, artistId = 13)
    val blackbird = track(8, "Blackbird", artist = "The Beatles", artistId = 14)
    val unknown = track(9, "Untitled")

    val library: List<Track> = listOf(backInBlack, blackbird, dejaVu, unknown, javoni, vatan, yolka, moiTwo, moi)
}
