package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.grouping.nameCollator
import tj.umar.navoplayer.core.domain.model.LibraryContent
import tj.umar.navoplayer.core.domain.model.SortedLibrary
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.repository.SettingsRepository
import tj.umar.navoplayer.core.domain.sorting.sortedFor
import javax.inject.Inject

class ObserveSortedLibraryUseCase @Inject constructor(
    private val observeLibrary: ObserveLibraryUseCase,
    private val settingsRepository: SettingsRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<SortedLibrary> =
        combine(
            observeLibrary(),
            settingsRepository.observeSettings()
                .catch { emit(UserSettings()) }
                .map { it.trackSort to it.groupSort }
                .distinctUntilChanged(),
        ) { library, (trackSort, groupSort) ->
            val collator = nameCollator()
            SortedLibrary(
                content = LibraryContent(
                    tracks = library.tracks.sortedFor(trackSort, collator),
                    albums = library.albums.sortedFor(groupSort, collator),
                    artists = library.artists.sortedFor(groupSort, collator),
                    folders = library.folders.sortedFor(groupSort, collator),
                ),
                trackSort = trackSort,
                groupSort = groupSort,
            )
        }
            .distinctUntilChanged()
            .flowOn(defaultDispatcher)
}
