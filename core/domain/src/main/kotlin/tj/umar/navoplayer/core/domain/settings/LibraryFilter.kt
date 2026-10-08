package tj.umar.navoplayer.core.domain.settings

import tj.umar.navoplayer.core.domain.grouping.nameCollator
import tj.umar.navoplayer.core.domain.model.LibraryFolder
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.UserSettings
import java.text.Collator

data class LibraryFilter(
    val minDurationMs: Long,
    val excludedFolders: Set<String>,
) {
    val isNoOp: Boolean
        get() = minDurationMs <= 0 && excludedFolders.isEmpty()

    fun accepts(track: Track): Boolean {
        val tooShort = track.durationMs in 1 until minDurationMs
        val excluded = track.folderPath != null && track.folderPath in excludedFolders
        return !tooShort && !excluded
    }
}

fun UserSettings.toLibraryFilter(): LibraryFilter =
    LibraryFilter(minDurationMs = minTrackDuration.millis, excludedFolders = excludedFolders)

fun List<Track>.filteredBy(filter: LibraryFilter): List<Track> =
    if (filter.isNoOp) this else filter(filter::accepts)

fun List<Track>.toLibraryFolders(excluded: Set<String>, collator: Collator = nameCollator()): List<LibraryFolder> =
    mapNotNull { it.folderPath }
        .groupingBy { it }
        .eachCount()
        .map { (path, count) ->
            LibraryFolder(
                path = path,
                name = path.trimEnd('/').substringAfterLast('/'),
                trackCount = count,
                isExcluded = path in excluded,
            )
        }
        .sortedWith(compareBy(collator) { it.name })
