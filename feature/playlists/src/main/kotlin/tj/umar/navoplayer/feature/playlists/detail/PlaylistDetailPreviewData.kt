package tj.umar.navoplayer.feature.playlists.detail

import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.domain.model.Track

private fun previewTrack(id: Long, title: String, artist: String, durationMs: Long) = Track(
    id = id,
    title = title,
    artist = artist,
    album = null,
    albumId = null,
    artistId = null,
    durationMs = durationMs,
    trackNumber = null,
    contentUri = "content://media/external/audio/media/$id",
    folderPath = "Music",
    discNumber = null,
    albumArtist = null,
)

internal val previewPlaylist = PlaylistDetail(
    id = 3,
    name = "Утро в горах",
    tracks = listOf(
        previewTrack(1, "Ҷавонӣ", "Daler Nazarov", 252_000),
        previewTrack(2, "Утро в Варзобе", "Navo Band", 185_000),
        previewTrack(3, "Памир", "Navo Band", 301_000),
    ),
    missingTrackCount = 2,
)
