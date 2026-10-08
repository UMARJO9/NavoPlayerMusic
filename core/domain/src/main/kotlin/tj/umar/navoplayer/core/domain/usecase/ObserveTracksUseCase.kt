package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.repository.SettingsRepository
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import tj.umar.navoplayer.core.domain.settings.filteredBy
import tj.umar.navoplayer.core.domain.settings.toLibraryFilter
import javax.inject.Inject

class ObserveTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<List<Track>> =
        combine(
            trackRepository.observeTracks(),
            settingsRepository.observeSettings().map { it.toLibraryFilter() }.distinctUntilChanged(),
        ) { tracks, filter -> tracks.filteredBy(filter) }
            .distinctUntilChanged()
}
