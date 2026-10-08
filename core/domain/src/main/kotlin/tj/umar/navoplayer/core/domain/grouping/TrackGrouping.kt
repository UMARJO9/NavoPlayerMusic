package tj.umar.navoplayer.core.domain.grouping

import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.Folder
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.domain.sorting.sortedFor
import java.text.Collator

fun nameCollator(): Collator = Collator.getInstance().apply { strength = Collator.PRIMARY }

fun Track.groupKey(type: TrackGroupType): TrackGroupKey = when (type) {
    TrackGroupType.Album -> namedKey(type, album, albumId)
    TrackGroupType.Artist -> namedKey(type, artist, artistId)
    TrackGroupType.Folder -> TrackGroupKey(type, id = null, name = folderPath)
}

fun List<Track>.toAlbums(collator: Collator = nameCollator()): List<Album> {
    val trackOrder = albumTrackOrder(CollationKeys(collator))
    return groupBy { it.groupKey(TrackGroupType.Album) }
        .map { (key, tracks) ->
            val taggedArtist = tracks.firstNotNullOfOrNull { it.albumArtist }
            val artists = tracks.mapNotNull { it.artist }.distinct()
            Album(
                key = key,
                title = tracks.firstNotNullOfOrNull { it.album },
                artist = taggedArtist ?: artists.singleOrNull(),
                hasVariousArtists = taggedArtist == null && artists.size > 1,
                tracks = tracks.sortedWith(trackOrder),
            )
        }
        .sortedFor(GroupSort.Default, collator)
}

fun List<Track>.toArtists(collator: Collator = nameCollator()): List<Artist> {
    val trackOrder = artistTrackOrder(CollationKeys(collator))
    return groupBy { it.groupKey(TrackGroupType.Artist) }
        .map { (key, tracks) ->
            Artist(
                key = key,
                name = tracks.firstNotNullOfOrNull { it.artist },
                albumCount = tracks.map { it.groupKey(TrackGroupType.Album) }.filterNot { it.isUnknown }.distinct().size,
                tracks = tracks.sortedWith(trackOrder),
            )
        }
        .sortedFor(GroupSort.Default, collator)
}

fun List<Track>.toFolders(collator: Collator = nameCollator()): List<Folder> {
    val trackOrder = titleTrackOrder(CollationKeys(collator))
    return groupBy { it.groupKey(TrackGroupType.Folder) }
        .map { (key, tracks) ->
            Folder(
                key = key,
                name = key.name?.substringAfterLast('/'),
                path = key.name,
                tracks = tracks.sortedWith(trackOrder),
            )
        }
        .sortedFor(GroupSort.Default, collator)
}

fun List<Track>.groupFor(key: TrackGroupKey, collator: Collator = nameCollator()): TrackGroup? {
    val members = filter { it.groupKey(key.type) == key }
    if (members.isEmpty()) return null
    return when (key.type) {
        TrackGroupType.Album -> members.toAlbums(collator).singleOrNull()
        TrackGroupType.Artist -> members.toArtists(collator).singleOrNull()
        TrackGroupType.Folder -> members.toFolders(collator).singleOrNull()
    }
}

private fun namedKey(type: TrackGroupType, name: String?, id: Long?): TrackGroupKey = when {
    name == null -> TrackGroupKey(type, id = null, name = null)
    id != null -> TrackGroupKey(type, id = id, name = null)
    else -> TrackGroupKey(type, id = null, name = name)
}

private fun titleTrackOrder(keys: CollationKeys): Comparator<Track> =
    compareBy<Track> { keys.of(it.title) }.thenBy { it.id }

private fun albumTrackOrder(keys: CollationKeys): Comparator<Track> =
    compareBy<Track> { it.discNumber ?: 0 }
        .thenBy(nullsLast()) { it.trackNumber }
        .then(titleTrackOrder(keys))

private fun artistTrackOrder(keys: CollationKeys): Comparator<Track> =
    compareBy<Track> { it.album == null }
        .thenBy { keys.of(it.album.orEmpty()) }
        .thenBy { it.albumId ?: Long.MAX_VALUE }
        .thenBy { it.discNumber ?: 0 }
        .thenBy { it.trackNumber ?: Int.MAX_VALUE }
        .then(titleTrackOrder(keys))
