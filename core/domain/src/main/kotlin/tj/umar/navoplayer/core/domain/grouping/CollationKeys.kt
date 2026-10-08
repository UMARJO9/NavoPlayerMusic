package tj.umar.navoplayer.core.domain.grouping

import java.text.CollationKey
import java.text.Collator

internal class CollationKeys(private val collator: Collator) {
    private val cache = HashMap<String, CollationKey>()

    fun of(text: String): CollationKey = cache.getOrPut(text) { collator.getCollationKey(text) }
}
