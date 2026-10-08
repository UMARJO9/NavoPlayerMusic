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
    companion object {
        val None = LibraryFilter(minDurationMs = 0, excludedFolders = emptySet())
    }

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

fun List<Track>.toLibraryFolders(excluded: Set<String>, collator: Collator = nameCollator()): List<LibraryFolder> {
    val counts = mapNotNull { it.folderPath }.groupingBy { it }.eachCount()
    val emptyExcluded = (excluded - counts.keys).associateWith { 0 }
    return (counts + emptyExcluded)
        .map { (path, count) ->
            LibraryFolder(
                path = path,
                name = path.trimEnd('/').substringAfterLast('/'),
                trackCount = count,
                isExcluded = path in excluded,
            )
        }
        .sortedWith(compareBy(collator) { it.name })
}

data class TrackCatalog(
    val tracksById: Map<Long, Track>,
    val filter: LibraryFilter = LibraryFilter.None,
) {
    fun visibleTracks(ids: List<Long>): List<Track> = ids.mapNotNull(tracksById::get).filter(filter::accepts)

    fun missingCount(ids: List<Long>): Int = ids.count { it !in tracksById }
}
