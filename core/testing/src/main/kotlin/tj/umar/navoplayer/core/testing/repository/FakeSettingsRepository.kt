package tj.umar.navoplayer.core.testing.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSortField
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.repository.SettingsRepository

class FakeSettingsRepository(initial: UserSettings = UserSettings()) : SettingsRepository {

    private val settings = MutableStateFlow(initial)

    var observeCalls: Int = 0
        private set

    var observeError: Throwable? = null

    var writeError: Throwable? = null

    var writeGate: CompletableDeferred<Unit>? = null

    var writeCalls: Int = 0
        private set

    val current: UserSettings
        get() = settings.value

    override fun observeSettings(): Flow<UserSettings> = flow {
        observeCalls++
        observeError?.let { throw it }
        emitAll(settings)
    }

    override suspend fun setMinTrackDuration(value: MinTrackDuration) {
        write { copy(minTrackDuration = value) }
    }

    override suspend fun setFolderExcluded(path: String, excluded: Boolean) {
        write { copy(excludedFolders = if (excluded) excludedFolders + path else excludedFolders - path) }
    }

    override suspend fun setPauseOnHeadphonesDisconnect(enabled: Boolean) {
        write { copy(pauseOnHeadphonesDisconnect = enabled) }
    }

    override suspend fun setTrackSortField(field: TrackSortField) {
        write { copy(trackSort = trackSort.copy(field = field)) }
    }

    override suspend fun setTrackSortDirection(direction: SortDirection) {
        write { copy(trackSort = trackSort.copy(direction = direction)) }
    }

    override suspend fun setGroupSortField(field: GroupSortField) {
        write { copy(groupSort = groupSort.copy(field = field)) }
    }

    override suspend fun setGroupSortDirection(direction: SortDirection) {
        write { copy(groupSort = groupSort.copy(direction = direction)) }
    }

    fun emit(value: UserSettings) {
        settings.value = value
    }

    private suspend fun write(transform: UserSettings.() -> UserSettings) {
        writeCalls++
        writeGate?.await()
        writeError?.let { throw it }
        settings.update(transform)
    }
}
