package tj.umar.navoplayer.core.mediastore.audio

import android.provider.MediaStore

internal const val MUSIC_SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

internal const val ID_SELECTION_CHUNK_SIZE = 500

internal data class IdSelection(val selection: String, val args: Array<String>) {
    override fun equals(other: Any?): Boolean =
        other is IdSelection && selection == other.selection && args.contentEquals(other.args)

    override fun hashCode(): Int = 31 * selection.hashCode() + args.contentHashCode()
}

internal fun idSelectionChunks(ids: Collection<Long>, size: Int = ID_SELECTION_CHUNK_SIZE): List<IdSelection> =
    ids.distinct().chunked(size).map { chunk ->
        val placeholders = chunk.joinToString(separator = ",") { "?" }
        IdSelection(
            selection = "$MUSIC_SELECTION AND ${MediaStore.Audio.Media._ID} IN ($placeholders)",
            args = chunk.map(Long::toString).toTypedArray(),
        )
    }
