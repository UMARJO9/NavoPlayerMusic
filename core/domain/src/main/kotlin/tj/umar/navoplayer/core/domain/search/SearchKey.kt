package tj.umar.navoplayer.core.domain.search

import java.text.Normalizer
import java.util.Locale

private const val ASCII_LIMIT = 0x7F

private val LetterFolds: Map<Char, String> = mapOf(
    'ё' to "е",
    'ӣ' to "и",
    'ӯ' to "у",
    'ҷ' to "ч",
    'ҳ' to "х",
    'қ' to "к",
    'ғ' to "г",
    'ß' to "ss",
    'æ' to "ae",
    'œ' to "oe",
    'ø' to "o",
    'ł' to "l",
    'đ' to "d",
    'ı' to "i",
)

private val Apostrophes: Set<Int> = setOf('\''.code, '’'.code, 'ʼ'.code, '`'.code, '‘'.code)

private val IgnoredMarkTypes: Set<Int> = setOf(
    Character.NON_SPACING_MARK.toInt(),
    Character.COMBINING_SPACING_MARK.toInt(),
    Character.ENCLOSING_MARK.toInt(),
)

internal fun String.toSearchKey(): String {
    val composed = Normalizer.normalize(this, Normalizer.Form.NFC).lowercase(Locale.ROOT)
    val folded = StringBuilder(composed.length)
    var index = 0
    while (index < composed.length) {
        val codePoint = composed.codePointAt(index)
        index += Character.charCount(codePoint)
        val fold = if (Character.isBmpCodePoint(codePoint)) LetterFolds[codePoint.toChar()] else null
        when {
            codePoint == 'й'.code -> folded.appendCodePoint(codePoint)
            fold != null -> folded.append(fold)
            codePoint in Apostrophes -> Unit
            Character.getType(codePoint) in IgnoredMarkTypes -> Unit
            !Character.isLetterOrDigit(codePoint) -> folded.append(' ')
            codePoint <= ASCII_LIMIT -> folded.appendCodePoint(codePoint)
            else -> folded.append(String(Character.toChars(codePoint)).withoutMarks())
        }
    }
    return folded.split(' ').filter { it.isNotEmpty() }.joinToString(" ")
}

internal fun String.toJoinedSearchKey(): String? {
    val hasInnerPunctuation = any { !it.isLetterOrDigit() && !it.isWhitespace() && it.code !in Apostrophes }
    if (!hasInnerPunctuation) return null
    return toSearchKey().replace(" ", "").takeIf { it.isNotEmpty() }
}

private fun String.withoutMarks(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .filterNot { Character.getType(it) in IgnoredMarkTypes }
