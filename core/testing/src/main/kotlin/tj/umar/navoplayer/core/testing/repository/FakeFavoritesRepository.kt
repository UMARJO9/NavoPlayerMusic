package tj.umar.navoplayer.core.testing.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository

class FakeFavoritesRepository(initial: List<Long> = emptyList()) : FavoritesRepository {

    private val favoriteIds = MutableStateFlow(initial)

    var observeCalls: Int = 0
        private set

    var observeError: Throwable? = null

    var writeError: Throwable? = null

    var writeGate: CompletableDeferred<Unit>? = null

    var writeCalls: Int = 0
        private set

    val current: List<Long>
        get() = favoriteIds.value

    override fun observeFavoriteIds(): Flow<List<Long>> = flow {
        observeCalls++
        observeError?.let { throw it }
        emitAll(favoriteIds)
    }

    override fun observeIsFavorite(trackId: Long): Flow<Boolean> = flow {
        observeError?.let { throw it }
        emitAll(favoriteIds.map { trackId in it }.distinctUntilChanged())
    }

    override suspend fun addFavorite(trackId: Long): Boolean {
        failIfNeeded()
        if (trackId in favoriteIds.value) return false
        favoriteIds.update { listOf(trackId) + it }
        return true
    }

    override suspend fun removeFavorite(trackId: Long): Boolean {
        failIfNeeded()
        if (trackId !in favoriteIds.value) return false
        favoriteIds.update { it - trackId }
        return true
    }

    private suspend fun failIfNeeded() {
        writeCalls++
        writeGate?.await()
        writeError?.let { throw it }
    }
}
