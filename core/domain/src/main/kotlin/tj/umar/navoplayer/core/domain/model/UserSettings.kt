package tj.umar.navoplayer.core.domain.model

private const val MILLIS_PER_SECOND = 1_000L

enum class MinTrackDuration(val seconds: Int) {
    Off(0),
    TenSeconds(10),
    ThirtySeconds(30),
    SixtySeconds(60),
    ;

    val millis: Long
        get() = seconds * MILLIS_PER_SECOND

    companion object {
        fun fromSeconds(seconds: Int): MinTrackDuration = entries.firstOrNull { it.seconds == seconds } ?: Off
    }
}

data class UserSettings(
    val minTrackDuration: MinTrackDuration = MinTrackDuration.Off,
    val excludedFolders: Set<String> = emptySet(),
    val pauseOnHeadphonesDisconnect: Boolean = true,
    val trackSort: TrackSort = TrackSort.Default,
    val groupSort: GroupSort = GroupSort.Default,
)

data class LibraryFolder(
    val path: String,
    val name: String,
    val trackCount: Int,
    val isExcluded: Boolean,
)

data class AppInfo(val versionName: String)
