package tj.umar.navoplayer.core.domain.model

sealed interface PlaybackSource {
    data object AllTracks : PlaybackSource
    data class Album(val title: String?) : PlaybackSource
    data class Artist(val name: String?) : PlaybackSource
    data class Folder(val name: String?) : PlaybackSource
    data class Search(val query: String) : PlaybackSource
    data class Playlist(val id: Long, val name: String) : PlaybackSource
    data object Favorites : PlaybackSource
    data object Queue : PlaybackSource
}

fun TrackGroup.toPlaybackSource(): PlaybackSource = when (this) {
    is Album -> PlaybackSource.Album(title)
    is Artist -> PlaybackSource.Artist(name)
    is Folder -> PlaybackSource.Folder(name)
}

fun PlaybackSource?.isPlaylist(id: Long): Boolean = this is PlaybackSource.Playlist && this.id == id

fun PlaybackSource?.isFavorites(): Boolean = this == PlaybackSource.Favorites
