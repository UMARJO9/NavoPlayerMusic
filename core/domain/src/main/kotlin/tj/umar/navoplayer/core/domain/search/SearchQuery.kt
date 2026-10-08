package tj.umar.navoplayer.core.domain.search

internal class SearchQuery private constructor(
    val text: String,
    val phrase: String,
    val tokens: List<String>,
) {
    val isBlank: Boolean
        get() = tokens.isEmpty()

    companion object {
        fun parse(raw: String): SearchQuery {
            val text = raw.trim()
            val phrase = text.toSearchKey()
            val tokens = if (phrase.isEmpty()) emptyList() else phrase.split(' ').distinct()
            return SearchQuery(text = text, phrase = phrase, tokens = tokens)
        }
    }
}
