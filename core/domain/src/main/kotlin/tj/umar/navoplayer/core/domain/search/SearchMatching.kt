package tj.umar.navoplayer.core.domain.search

internal enum class MatchTier { Exact, Prefix, WordPrefix, Substring, Scattered }

internal data class SearchField(val key: String, val rank: Int)

internal data class SearchMatch(val tier: MatchTier, val fieldRank: Int)

internal class SearchEntry<T>(val item: T, val fields: List<SearchField>) {

    fun match(query: SearchQuery): SearchMatch? {
        if (query.isBlank) return null
        val matchedFields = query.tokens.map { token ->
            fields.filter { it.key.containsToken(token) }.ifEmpty { return null }
        }.flatten()
        val wholePhrase = fields.mapNotNull { field ->
            field.key.phraseTier(query.phrase)?.let { SearchMatch(it, field.rank) }
        }.minWithOrNull(compareBy({ it.tier }, { it.fieldRank }))
        return wholePhrase ?: SearchMatch(MatchTier.Scattered, matchedFields.minOf { it.rank })
    }
}

internal fun <T> List<SearchEntry<T>>.search(query: SearchQuery): List<T> =
    mapNotNull { entry -> entry.match(query)?.let { entry.item to it } }
        .sortedWith(compareBy({ it.second.tier }, { it.second.fieldRank }))
        .map { it.first }

private fun String.containsToken(token: String): Boolean =
    if (token.length == 1) startsWith(token) || contains(" $token") else contains(token)

private fun String.phraseTier(phrase: String): MatchTier? = when {
    this == phrase -> MatchTier.Exact
    startsWith(phrase) -> MatchTier.Prefix
    contains(" $phrase") -> MatchTier.WordPrefix
    phrase.length > 1 && contains(phrase) -> MatchTier.Substring
    else -> null
}
