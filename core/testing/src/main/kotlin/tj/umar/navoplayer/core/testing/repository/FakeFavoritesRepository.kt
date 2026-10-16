package tj.umar.navoplayer.core.testing.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import tj.umar.navoplayer.core.domain.model.RemovedFavorite
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository

class FakeFavoritesRepository(initial: List<Long> = emptyList()) : FavoritesRepository {

    private val entries = MutableStateFlow(
        initial.distinct().mapIndexed { index, trackId -> RemovedFavorite(trackId, (initial.size - index).toLong()) },
    )
    private var clock = initial.size.toLong() + 1

    private val favoriteIds: Flow<List<Long>> = entries.map { it.sortedIds() }

    var observeCalls: Int = 0
        private set

    var observeError: Throwable? = null

    var writeError: Throwable? = null

    var writeGate: CompletableDeferred<Unit>? = null

    var writeCalls: Int = 0
        private set

    val restoreCalls: MutableList<RemovedFavorite> = mutableListOf()

    val current: List<Long>
        get() = entries.value.sortedIds()

    override fun observeFavoriteIds(): Flow<List<Long>> = flow {
        observeCalls++
        observeError?.let { throw it }
        emitAll(favoriteIds.distinctUntilChanged())
    }

    override fun observeIsFavorite(trackId: Long): Flow<Boolean> = flow {
        observeError?.let { throw it }
        emitAll(favoriteIds.map { trackId in it }.distinctUntilChanged())
    }

    override suspend fun addFavorite(trackId: Long): Boolean {
        failIfNeeded()
        if (entries.value.any { it.trackId == trackId }) return false
        val addedAt = clock++
        entries.update { it + RemovedFavorite(trackId, addedAt) }
        return true
    }

    override suspend fun removeFavorite(trackId: Long): RemovedFavorite? {
        failIfNeeded()
        val entry = entries.value.firstOrNull { it.trackId == trackId } ?: return null
        entries.update { it - entry }
        return entry
    }

    override suspend fun restoreFavorite(removed: RemovedFavorite): Boolean {
        restoreCalls += removed
        failIfNeeded()
        if (entries.value.any { it.trackId == removed.trackId }) return false
        entries.update { it + removed }
        return true
    }

    private fun List<RemovedFavorite>.sortedIds(): List<Long> =
        sortedWith(compareByDescending<RemovedFavorite> { it.addedAtMs }.thenByDescending { it.trackId }).map { it.trackId }

    private suspend fun failIfNeeded() {
        writeCalls++
        writeGate?.await()
        writeError?.let { throw it }
    }
}
