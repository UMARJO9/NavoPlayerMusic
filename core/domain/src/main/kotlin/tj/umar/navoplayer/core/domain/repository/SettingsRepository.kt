package tj.umar.navoplayer.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings

interface SettingsRepository {

    fun observeSettings(): Flow<UserSettings>

    suspend fun setMinTrackDuration(value: MinTrackDuration)

    suspend fun setFolderExcluded(path: String, excluded: Boolean)

    suspend fun setPauseOnHeadphonesDisconnect(enabled: Boolean)
}
