package tj.umar.navoplayer.core.domain.search

import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.LibraryContent
import tj.umar.navoplayer.core.domain.model.SearchResults
import tj.umar.navoplayer.core.domain.model.Track

private const val PRIMARY_RANK = 0
private const val ARTIST_RANK = 1
private const val ALBUM_RANK = 2

internal class LibrarySearchIndex(content: LibraryContent) {

    private val tracks: List<SearchEntry<Track>> = content.tracks.map { track ->
        SearchEntry(
            item = track,
            fields = fieldsOf(
                track.title to PRIMARY_RANK,
                track.artist to ARTIST_RANK,
                track.albumArtist to ARTIST_RANK,
                track.album to ALBUM_RANK,
            ),
        )
    }

    private val albums: List<SearchEntry<Album>> = content.albums
        .filterNot { it.key.isUnknown }
        .map { album -> SearchEntry(album, fieldsOf(album.title to PRIMARY_RANK, album.artist to ARTIST_RANK)) }

    private val artists: List<SearchEntry<Artist>> = content.artists
        .filterNot { it.key.isUnknown }
        .map { artist -> SearchEntry(artist, fieldsOf(artist.name to PRIMARY_RANK)) }

    fun search(query: SearchQuery): SearchResults {
        if (query.isBlank) return SearchResults.empty(query.text)
        return SearchResults(
            query = query.text,
            tracks = tracks.search(query),
            albums = albums.search(query),
            artists = artists.search(query),
        )
    }
}

private fun fieldsOf(vararg values: Pair<String?, Int>): List<SearchField> =
    values.mapNotNull { (text, rank) ->
        text?.toSearchKey()?.takeIf { it.isNotEmpty() }?.let { SearchField(it, rank) }
    }
