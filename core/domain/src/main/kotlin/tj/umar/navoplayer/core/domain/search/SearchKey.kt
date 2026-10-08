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

internal fun String.toSearchKey(): String {
    val composed = Normalizer.normalize(this, Normalizer.Form.NFC).lowercase(Locale.ROOT)
    val folded = StringBuilder(composed.length)
    for (char in composed) {
        when {
            char == 'й' -> folded.append(char)
            char in LetterFolds -> folded.append(LetterFolds.getValue(char))
            Character.getType(char) == Character.NON_SPACING_MARK.toInt() -> Unit
            !char.isLetterOrDigit() -> folded.append(' ')
            char.code <= ASCII_LIMIT -> folded.append(char)
            else -> folded.append(char.withoutMarks())
        }
    }
    return folded.split(' ').filter { it.isNotEmpty() }.joinToString(" ")
}

private fun Char.withoutMarks(): String =
    Normalizer.normalize(toString(), Normalizer.Form.NFD)
        .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
