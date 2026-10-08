package tj.umar.navoplayer.core.domain.sorting

import tj.umar.navoplayer.core.domain.grouping.CollationKeys
import tj.umar.navoplayer.core.domain.grouping.nameCollator
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackSort
import tj.umar.navoplayer.core.domain.model.TrackSortField
import java.text.CollationKey
import java.text.Collator

fun List<Track>.sortedFor(sort: TrackSort, collator: Collator = nameCollator()): List<Track> =
    sortedWith(trackComparator(sort, CollationKeys(collator)))

internal fun trackComparator(sort: TrackSort, keys: CollationKeys): Comparator<Track> {
    val byTitle = compareBy<Track> { it.title.isBlank() }.thenBy { keys.of(it.title) }
    val tail = byTitle.thenBy { it.id }
    val main: Comparator<Track> = when (sort.field) {
        TrackSortField.Title -> keyed<Track, CollationKey>(sort.direction, { it.title.isBlank() }, { keys.of(it.title) })
            .thenBy { it.artist == null }
            .thenBy { keys.of(it.artist.orEmpty()) }
            .thenBy { it.album == null }
            .thenBy { keys.of(it.album.orEmpty()) }
            .thenBy { it.id }
        TrackSortField.Artist -> keyed<Track, CollationKey>(sort.direction, { it.artist == null }, { keys.of(it.artist.orEmpty()) })
            .thenBy { it.album == null }
            .thenBy { keys.of(it.album.orEmpty()) }
            .thenBy { it.discNumber ?: 0 }
            .thenBy { it.trackNumber ?: Int.MAX_VALUE }
            .then(tail)
        TrackSortField.Album -> keyed<Track, CollationKey>(sort.direction, { it.album == null }, { keys.of(it.album.orEmpty()) })
            .thenBy { it.albumId ?: Long.MAX_VALUE }
            .thenBy { it.discNumber ?: 0 }
            .thenBy { it.trackNumber ?: Int.MAX_VALUE }
            .then(tail)
        TrackSortField.DateAdded -> keyed<Track, Long>(sort.direction, { it.dateAddedMs == null }, { it.dateAddedMs ?: 0L })
            .then(tail)
        TrackSortField.Duration -> keyed<Track, Long>(sort.direction, { it.durationMs <= 0 }, { it.durationMs })
            .then(tail)
    }
    return main
}

internal fun <T, K : Comparable<K>> keyed(
    direction: SortDirection,
    isMissing: (T) -> Boolean,
    key: (T) -> K,
): Comparator<T> {
    val byKey = compareBy(key)
    return compareBy(isMissing).then(if (direction == SortDirection.Descending) byKey.reversed() else byKey)
}
