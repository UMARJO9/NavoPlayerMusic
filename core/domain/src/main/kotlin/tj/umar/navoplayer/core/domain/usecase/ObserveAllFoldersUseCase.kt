package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.model.LibraryFolder
import tj.umar.navoplayer.core.domain.repository.SettingsRepository
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import tj.umar.navoplayer.core.domain.settings.toLibraryFolders
import javax.inject.Inject

class ObserveAllFoldersUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
    private val settingsRepository: SettingsRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<List<LibraryFolder>> =
        combine(
            trackRepository.observeTracks(),
            settingsRepository.observeSettings().map { it.excludedFolders }.distinctUntilChanged(),
        ) { tracks, excluded -> tracks.toLibraryFolders(excluded) }
            .flowOn(defaultDispatcher)
            .distinctUntilChanged()
}
