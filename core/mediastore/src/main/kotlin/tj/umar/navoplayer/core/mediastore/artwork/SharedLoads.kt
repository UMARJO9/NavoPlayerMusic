package tj.umar.navoplayer.core.mediastore.artwork

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class SharedLoads<K : Any, V>(
    private val scope: CoroutineScope,
    private val context: CoroutineContext = EmptyCoroutineContext,
) {

    private val supervisor = SupervisorJob(scope.coroutineContext[Job])
    private val lock = Any()
    private val entries = HashMap<K, Entry<V>>()

    suspend fun load(key: K, block: suspend () -> V): V {
        val entry = synchronized(lock) {
            val current = entries[key] ?: start(key, block)
            current.waiters++
            current
        }
        try {
            return entry.deferred.await()
        } finally {
            synchronized(lock) {
                entry.waiters--
                if (entry.waiters == 0) {
                    if (entries[key] === entry) entries.remove(key)
                    entry.deferred.cancel()
                }
            }
        }
    }

    private fun start(key: K, block: suspend () -> V): Entry<V> {
        val deferred = scope.async(context + supervisor, start = CoroutineStart.LAZY) { block() }
        val entry = Entry(deferred)
        entries[key] = entry
        deferred.invokeOnCompletion {
            synchronized(lock) {
                if (entries[key] === entry) entries.remove(key)
            }
        }
        return entry
    }

    private class Entry<V>(val deferred: Deferred<V>) {
        var waiters: Int = 0
    }
}
