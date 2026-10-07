package tj.umar.navoplayer.core.data.repository

import tj.umar.navoplayer.core.domain.model.Track
import java.text.Collator

internal fun trackTitleComparator(collator: Collator = Collator.getInstance()): Comparator<Track> {
    collator.strength = Collator.PRIMARY
    return Comparator<Track> { first, second -> collator.compare(first.title, second.title) }
        .thenBy { it.id }
}
