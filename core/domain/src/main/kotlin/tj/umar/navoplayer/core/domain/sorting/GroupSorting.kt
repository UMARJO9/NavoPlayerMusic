package tj.umar.navoplayer.core.domain.sorting

import tj.umar.navoplayer.core.domain.grouping.CollationKeys
import tj.umar.navoplayer.core.domain.grouping.nameCollator
import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.Folder
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.TrackGroup
import java.text.CollationKey
import java.text.Collator

fun <G : TrackGroup> List<G>.sortedFor(sort: GroupSort, collator: Collator = nameCollator()): List<G> {
    val keys = CollationKeys(collator)
    val byName = compareBy<G> { it.key.isUnknown }.thenBy { keys.of(it.sortName.orEmpty()) }
    val main: Comparator<G> = when (sort.field) {
        GroupSortField.Name -> keyed<G, CollationKey>(sort.direction, { it.key.isUnknown }, { keys.of(it.sortName.orEmpty()) })
        GroupSortField.TrackCount -> keyed<G, Int>(sort.direction, { false }, { it.tracks.size }).then(byName)
    }
    return sortedWith(
        main
            .thenBy { it.key.id ?: Long.MAX_VALUE }
            .thenBy { it.key.name.orEmpty() },
    )
}

internal val TrackGroup.sortName: String?
    get() = when (this) {
        is Album -> title
        is Artist -> name
        is Folder -> name
    }
