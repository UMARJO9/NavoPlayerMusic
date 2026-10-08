package tj.umar.navoplayer.core.domain.grouping

import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.Folder
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import java.text.CollationKey
import java.text.Collator

fun nameCollator(): Collator = Collator.getInstance().apply { strength = Collator.PRIMARY }

fun Track.groupKey(type: TrackGroupType): TrackGroupKey = when (type) {
    TrackGroupType.Album -> namedKey(type, album, albumId)
    TrackGroupType.Artist -> namedKey(type, artist, artistId)
    TrackGroupType.Folder -> TrackGroupKey(type, id = null, name = folderPath)
}

fun List<Track>.toAlbums(collator: Collator = nameCollator()): List<Album> {
    val trackOrder = albumTrackOrder(collator)
    return groupBy { it.groupKey(TrackGroupType.Album) }
        .map { (key, tracks) ->
            val artists = tracks.mapNotNull { it.artist }.distinct()
            Album(
                key = key,
                title = tracks.firstNotNullOfOrNull { it.album },
                artist = artists.singleOrNull(),
                hasVariousArtists = artists.size > 1,
                tracks = tracks.sortedWith(trackOrder),
            )
        }
        .sortedByName(collator) { it.title }
}

fun List<Track>.toArtists(collator: Collator = nameCollator()): List<Artist> {
    val trackOrder = artistTrackOrder(collator)
    return groupBy { it.groupKey(TrackGroupType.Artist) }
        .map { (key, tracks) ->
            Artist(
                key = key,
                name = tracks.firstNotNullOfOrNull { it.artist },
                albumCount = tracks.map { it.groupKey(TrackGroupType.Album) }.filterNot { it.isUnknown }.distinct().size,
                tracks = tracks.sortedWith(trackOrder),
            )
        }
        .sortedByName(collator) { it.name }
}

fun List<Track>.toFolders(collator: Collator = nameCollator()): List<Folder> {
    val trackOrder = titleTrackOrder(collator)
    return groupBy { it.groupKey(TrackGroupType.Folder) }
        .map { (key, tracks) ->
            Folder(
                key = key,
                name = key.name?.substringAfterLast('/'),
                path = key.name,
                tracks = tracks.sortedWith(trackOrder),
            )
        }
        .sortedByName(collator) { it.name }
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

private fun <G : TrackGroup> List<G>.sortedByName(collator: Collator, displayName: (G) -> String?): List<G> {
    val collationKeys: Map<G, CollationKey> = associateWith { collator.getCollationKey(displayName(it).orEmpty()) }
    return sortedWith(
        compareBy<G> { it.key.isUnknown }
            .thenBy { collationKeys.getValue(it) }
            .thenBy { it.key.id ?: Long.MAX_VALUE }
            .thenBy { it.key.name.orEmpty() },
    )
}

private fun titleTrackOrder(collator: Collator): Comparator<Track> =
    Comparator<Track> { first, second -> collator.compare(first.title, second.title) }
        .thenBy { it.id }

private fun albumTrackOrder(collator: Collator): Comparator<Track> =
    compareBy<Track> { it.discNumber ?: 0 }
        .thenBy(nullsLast()) { it.trackNumber }
        .then(titleTrackOrder(collator))

private fun artistTrackOrder(collator: Collator): Comparator<Track> =
    compareBy<Track> { it.album == null }
        .then(Comparator { first, second -> collator.compare(first.album.orEmpty(), second.album.orEmpty()) })
        .thenBy { it.discNumber ?: 0 }
        .thenComparator { first, second -> compareValues(first.trackNumber ?: Int.MAX_VALUE, second.trackNumber ?: Int.MAX_VALUE) }
        .then(titleTrackOrder(collator))
