package tj.umar.navoplayer.core.testing.data

import tj.umar.navoplayer.core.domain.model.Track

object TestTracks {

    val alpha = Track(
        id = 1,
        title = "Alpha",
        artist = "Navo Band",
        album = "First",
        albumId = 10,
        artistId = 100,
        durationMs = 185_000,
        trackNumber = 1,
        contentUri = "content://media/external/audio/media/1",
    )

    val beta = Track(
        id = 2,
        title = "beta",
        artist = null,
        album = null,
        albumId = null,
        artistId = null,
        durationMs = 42_000,
        trackNumber = null,
        contentUri = "content://media/external/audio/media/2",
    )

    val longMix = Track(
        id = 3,
        title = "Long Mix",
        artist = "DJ Navo",
        album = "Mixes",
        albumId = 11,
        artistId = 101,
        durationMs = 3_725_000,
        trackNumber = 2,
        contentUri = "content://media/external/audio/media/3",
    )

    val tracks: List<Track> = listOf(alpha, beta, longMix)
}
