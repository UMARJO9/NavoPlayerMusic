package tj.umar.navoplayer.core.domain.playlist

const val PLAYLIST_NAME_MAX_LENGTH = 100

private val InnerWhitespace = Regex("\\s+")

fun normalizePlaylistName(raw: String): String? =
    raw.trim()
        .replace(InnerWhitespace, " ")
        .takeIf { it.isNotEmpty() && it.length <= PLAYLIST_NAME_MAX_LENGTH }
