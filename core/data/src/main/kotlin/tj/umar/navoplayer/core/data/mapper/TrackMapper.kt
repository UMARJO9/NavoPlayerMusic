package tj.umar.navoplayer.core.data.mapper

import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.mediastore.audio.MediaStoreAudioRow

private const val UNKNOWN_TAG = "<unknown>"
private const val DISC_TRACK_DIVIDER = 1000

internal fun MediaStoreAudioRow.toTrack(): Track = Track(
    id = id,
    title = cleanTag(title) ?: titleFromFileName(displayName),
    artist = cleanTag(artist),
    album = cleanTag(album),
    albumId = albumId?.takeIf { it > 0 },
    artistId = artistId?.takeIf { it > 0 },
    durationMs = durationMs?.coerceAtLeast(0) ?: 0,
    trackNumber = track?.takeIf { it > 0 }?.rem(DISC_TRACK_DIVIDER)?.takeIf { it > 0 },
    contentUri = contentUri,
    folderPath = folderPathOf(relativePath, dataPath),
    discNumber = track?.takeIf { it >= DISC_TRACK_DIVIDER }?.div(DISC_TRACK_DIVIDER),
)

private fun cleanTag(value: String?): String? =
    value?.trim()?.takeUnless { it.isEmpty() || it.equals(UNKNOWN_TAG, ignoreCase = true) }

private fun titleFromFileName(displayName: String?): String =
    displayName?.substringBeforeLast('.')?.trim().orEmpty()

private val StorageRootPrefix = Regex("""^/storage/(emulated/\d+|[^/]+)/?|^/sdcard/?""")

internal fun folderPathOf(relativePath: String?, dataPath: String?): String? {
    if (relativePath != null) return relativePath.trim().trimEnd('/').ifBlank { null }
    val directory = dataPath?.trim()?.substringBeforeLast('/', missingDelimiterValue = "") ?: return null
    return directory.replaceFirst(StorageRootPrefix, "").trimEnd('/').ifBlank { null }
}
