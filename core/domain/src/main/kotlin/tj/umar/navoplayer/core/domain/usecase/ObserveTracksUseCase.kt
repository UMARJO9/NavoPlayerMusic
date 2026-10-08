package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.playlist.indexById
import tj.umar.navoplayer.core.domain.repository.SettingsRepository
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import tj.umar.navoplayer.core.domain.settings.LibraryFilter
import tj.umar.navoplayer.core.domain.settings.TrackCatalog
import tj.umar.navoplayer.core.domain.settings.filteredBy
import tj.umar.navoplayer.core.domain.settings.toLibraryFilter
import javax.inject.Inject

class ObserveTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<List<Track>> =
        combine(trackRepository.observeTracks(), filters()) { tracks, filter -> tracks.filteredBy(filter) }
            .distinctUntilChanged()

    fun catalog(): Flow<TrackCatalog> =
        combine(trackRepository.observeTracks(), filters()) { tracks, filter -> TrackCatalog(tracks.indexById(), filter) }
            .distinctUntilChanged()

    private fun filters(): Flow<LibraryFilter> =
        settingsRepository.observeSettings()
            .catch { emit(UserSettings()) }
            .map { it.toLibraryFilter() }
            .distinctUntilChanged()
}
