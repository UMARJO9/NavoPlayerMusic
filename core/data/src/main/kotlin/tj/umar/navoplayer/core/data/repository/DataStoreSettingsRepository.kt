package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.datastore.settings.SettingsPreferencesDataSource
import tj.umar.navoplayer.core.datastore.settings.StoredSettings
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.repository.SettingsRepository
import javax.inject.Inject

internal class DataStoreSettingsRepository @Inject constructor(
    private val dataSource: SettingsPreferencesDataSource,
) : SettingsRepository {

    override fun observeSettings(): Flow<UserSettings> =
        dataSource.settings.map { it.toUserSettings() }.distinctUntilChanged()

    override suspend fun setMinTrackDuration(value: MinTrackDuration) {
        dataSource.setMinTrackDurationSeconds(value.seconds)
    }

    override suspend fun setFolderExcluded(path: String, excluded: Boolean) {
        dataSource.setFolderExcluded(path, excluded)
    }

    override suspend fun setPauseOnHeadphonesDisconnect(enabled: Boolean) {
        dataSource.setPauseOnHeadphonesDisconnect(enabled)
    }
}

internal fun StoredSettings.toUserSettings(): UserSettings = UserSettings(
    minTrackDuration = MinTrackDuration.fromSeconds(minTrackDurationSeconds),
    excludedFolders = excludedFolders,
    pauseOnHeadphonesDisconnect = pauseOnHeadphonesDisconnect,
)
