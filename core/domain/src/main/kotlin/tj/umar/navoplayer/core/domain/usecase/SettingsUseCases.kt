package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSortField
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.repository.SettingsRepository
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<UserSettings> = settingsRepository.observeSettings()
}

class SetMinTrackDurationUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(value: MinTrackDuration): NavoResult<Unit> =
        navoRunCatching { settingsRepository.setMinTrackDuration(value) }
}

class SetFolderExcludedUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(path: String, excluded: Boolean): NavoResult<Unit> =
        navoRunCatching { settingsRepository.setFolderExcluded(path, excluded) }
}

class SetPauseOnHeadphonesDisconnectUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): NavoResult<Unit> =
        navoRunCatching { settingsRepository.setPauseOnHeadphonesDisconnect(enabled) }
}

class SetTrackSortUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(field: TrackSortField): NavoResult<Unit> =
        navoRunCatching { settingsRepository.setTrackSortField(field) }

    suspend operator fun invoke(direction: SortDirection): NavoResult<Unit> =
        navoRunCatching { settingsRepository.setTrackSortDirection(direction) }
}

class SetGroupSortUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(field: GroupSortField): NavoResult<Unit> =
        navoRunCatching { settingsRepository.setGroupSortField(field) }

    suspend operator fun invoke(direction: SortDirection): NavoResult<Unit> =
        navoRunCatching { settingsRepository.setGroupSortDirection(direction) }
}
