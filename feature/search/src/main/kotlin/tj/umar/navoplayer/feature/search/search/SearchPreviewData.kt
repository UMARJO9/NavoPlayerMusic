package tj.umar.navoplayer.feature.search.search

import tj.umar.navoplayer.core.domain.grouping.toAlbums
import tj.umar.navoplayer.core.domain.grouping.toArtists
import tj.umar.navoplayer.core.domain.model.Track

private val previewTracks = listOf(
    Track(1, "Ҷавонӣ", "Daler Nazarov", "Ватан", 10, 100, 252_000, 1, "content://media/1", "Music", null, null),
    Track(2, "Ватан", "Daler Nazarov", "Ватан", 10, 100, 198_000, 2, "content://media/2", "Music", null, null),
    Track(3, "Ёлка", "Елена", null, null, 101, 185_000, null, "content://media/3", "Download", null, null),
)

internal val previewSearchState = SearchState(
    query = "ватан",
    phase = SearchPhase.Results,
    resultsQuery = "ватан",
    tracks = previewTracks.take(2),
    albums = previewTracks.toAlbums().filter { it.title == "Ватан" },
    artists = previewTracks.toArtists().filter { it.name == "Daler Nazarov" },
    currentTrackId = 1,
    isPlaying = true,
)
