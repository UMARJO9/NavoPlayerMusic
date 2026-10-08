package tj.umar.navoplayer.core.datastore.settings

data class StoredSettings(
    val minTrackDurationSeconds: Int,
    val excludedFolders: Set<String>,
    val pauseOnHeadphonesDisconnect: Boolean,
)
