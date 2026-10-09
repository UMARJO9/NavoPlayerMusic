package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.data.mapper.directionOf
import tj.umar.navoplayer.core.data.mapper.groupSortFieldOf
import tj.umar.navoplayer.core.data.mapper.storageValue
import tj.umar.navoplayer.core.data.mapper.trackSortFieldOf
import tj.umar.navoplayer.core.datastore.settings.SettingsPreferencesDataSource
import tj.umar.navoplayer.core.datastore.settings.StoredSettings
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSort
import tj.umar.navoplayer.core.domain.model.TrackSortField
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

    override suspend fun setTrackSortField(field: TrackSortField) {
        dataSource.setTrackSortField(field.storageValue())
    }

    override suspend fun setTrackSortDirection(direction: SortDirection) {
        dataSource.setTrackSortDescending(direction == SortDirection.Descending)
    }

    override suspend fun setGroupSortField(field: GroupSortField) {
        dataSource.setGroupSortField(field.storageValue())
    }

    override suspend fun setGroupSortDirection(direction: SortDirection) {
        dataSource.setGroupSortDescending(direction == SortDirection.Descending)
    }

    override suspend fun setPauseOnHeadphonesDisconnect(enabled: Boolean) {
        dataSource.setPauseOnHeadphonesDisconnect(enabled)
    }
}

internal fun StoredSettings.toUserSettings(): UserSettings = UserSettings(
    minTrackDuration = MinTrackDuration.fromSeconds(minTrackDurationSeconds),
    excludedFolders = excludedFolders,
    pauseOnHeadphonesDisconnect = pauseOnHeadphonesDisconnect,
    trackSort = TrackSort(trackSortFieldOf(trackSortField), directionOf(trackSortDescending)),
    groupSort = GroupSort(groupSortFieldOf(groupSortField), directionOf(groupSortDescending)),
)
