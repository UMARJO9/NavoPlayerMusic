package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.grouping.nameCollator
import tj.umar.navoplayer.core.domain.grouping.toAlbums
import tj.umar.navoplayer.core.domain.grouping.toArtists
import tj.umar.navoplayer.core.domain.grouping.toFolders
import tj.umar.navoplayer.core.domain.model.LibraryContent
import javax.inject.Inject

class ObserveLibraryUseCase @Inject constructor(
    private val observeTracks: ObserveTracksUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<LibraryContent> = observeTracks()
        .map { tracks ->
            val collator = nameCollator()
            LibraryContent(
                tracks = tracks,
                albums = tracks.toAlbums(collator),
                artists = tracks.toArtists(collator),
                folders = tracks.toFolders(collator),
            )
        }
        .flowOn(defaultDispatcher)
}
