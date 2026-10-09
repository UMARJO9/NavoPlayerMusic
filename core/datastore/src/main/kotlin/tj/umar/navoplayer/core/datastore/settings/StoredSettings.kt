package tj.umar.navoplayer.core.datastore.settings

data class StoredSettings(
    val minTrackDurationSeconds: Int,
    val excludedFolders: Set<String>,
    val pauseOnHeadphonesDisconnect: Boolean,
    val trackSortField: String? = null,
    val trackSortDescending: Boolean = false,
    val groupSortField: String? = null,
    val groupSortDescending: Boolean = false,
)
