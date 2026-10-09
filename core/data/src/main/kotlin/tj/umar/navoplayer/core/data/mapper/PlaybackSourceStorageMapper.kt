package tj.umar.navoplayer.core.data.mapper

import tj.umar.navoplayer.core.domain.model.PlaybackSource

private const val KIND_ALL_TRACKS = "all_tracks"
private const val KIND_ALBUM = "album"
private const val KIND_ARTIST = "artist"
private const val KIND_FOLDER = "folder"
private const val KIND_SEARCH = "search"
private const val KIND_PLAYLIST = "playlist"
private const val KIND_FAVORITES = "favorites"
private const val KIND_QUEUE = "queue"

internal data class StoredPlaybackSource(val kind: String?, val playlistId: Long?, val label: String?)

internal fun PlaybackSource?.toStored(): StoredPlaybackSource = when (this) {
    null -> StoredPlaybackSource(null, null, null)
    PlaybackSource.AllTracks -> StoredPlaybackSource(KIND_ALL_TRACKS, null, null)
    is PlaybackSource.Album -> StoredPlaybackSource(KIND_ALBUM, null, title)
    is PlaybackSource.Artist -> StoredPlaybackSource(KIND_ARTIST, null, name)
    is PlaybackSource.Folder -> StoredPlaybackSource(KIND_FOLDER, null, name)
    is PlaybackSource.Search -> StoredPlaybackSource(KIND_SEARCH, null, query)
    is PlaybackSource.Playlist -> StoredPlaybackSource(KIND_PLAYLIST, id, name)
    PlaybackSource.Favorites -> StoredPlaybackSource(KIND_FAVORITES, null, null)
    PlaybackSource.Queue -> StoredPlaybackSource(KIND_QUEUE, null, null)
}

internal fun StoredPlaybackSource.toPlaybackSource(): PlaybackSource? = when (kind) {
    KIND_ALL_TRACKS -> PlaybackSource.AllTracks
    KIND_ALBUM -> PlaybackSource.Album(label)
    KIND_ARTIST -> PlaybackSource.Artist(label)
    KIND_FOLDER -> PlaybackSource.Folder(label)
    KIND_SEARCH -> label?.let(PlaybackSource::Search)
    KIND_PLAYLIST -> if (playlistId != null && label != null) PlaybackSource.Playlist(playlistId, label) else null
    KIND_FAVORITES -> PlaybackSource.Favorites
    KIND_QUEUE -> PlaybackSource.Queue
    else -> null
}
