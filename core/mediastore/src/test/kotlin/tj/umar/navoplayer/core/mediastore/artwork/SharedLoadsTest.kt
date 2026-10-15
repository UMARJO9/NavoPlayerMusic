package tj.umar.navoplayer.core.mediastore.artwork

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedLoadsTest {

    @Test
    fun `two waiters share one execution`() = runTest {
        val loads = SharedLoads<Int, String>(backgroundScope)
        val gate = CompletableDeferred<Unit>()
        var executions = 0
        val block: suspend () -> String = {
            executions++
            gate.await()
            "art"
        }

        val first = async { loads.load(1, block) }
        val second = async { loads.load(1, block) }
        runCurrent()
        gate.complete(Unit)

        assertEquals("art", first.await())
        assertEquals("art", second.await())
        assertEquals(1, executions)
    }

    @Test
    fun `different keys run separately`() = runTest {
        val loads = SharedLoads<Int, Int>(backgroundScope)
        var executions = 0

        val first = async { loads.load(1) { executions++; 1 } }
        val second = async { loads.load(2) { executions++; 2 } }

        assertEquals(1, first.await())
        assertEquals(2, second.await())
        assertEquals(2, executions)
    }

    @Test
    fun `cancelling one waiter keeps the load for the other`() = runTest {
        val loads = SharedLoads<Int, String>(backgroundScope)
        val gate = CompletableDeferred<Unit>()
        var executions = 0
        val block: suspend () -> String = {
            executions++
            gate.await()
            "art"
        }

        val first = async { loads.load(1, block) }
        val second = async { loads.load(1, block) }
        runCurrent()
        first.cancel()
        runCurrent()
        gate.complete(Unit)

        assertEquals("art", second.await())
        assertTrue(first.isCancelled)
        assertEquals(1, executions)
    }

    @Test
    fun `cancelling every waiter cancels the load`() = runTest {
        val loads = SharedLoads<Int, String>(backgroundScope)
        var cancelled = false
        var completed = false
        val block: suspend () -> String = {
            try {
                CompletableDeferred<Unit>().await()
                completed = true
                "art"
            } finally {
                cancelled = !completed
            }
        }

        val first = async { loads.load(1, block) }
        val second = async { loads.load(1, block) }
        runCurrent()
        first.cancel()
        second.cancel()
        runCurrent()

        assertTrue(cancelled)
        assertFalse(completed)
    }

    @Test
    fun `failure reaches every waiter and next load retries`() = runTest {
        val loads = SharedLoads<Int, String>(backgroundScope)
        val gate = CompletableDeferred<Unit>()
        var executions = 0
        val failing: suspend () -> String = {
            executions++
            gate.await()
            throw IllegalStateException("broken")
        }

        val first = async(start = CoroutineStart.UNDISPATCHED) { runCatching { loads.load(1, failing) } }
        val second = async(start = CoroutineStart.UNDISPATCHED) { runCatching { loads.load(1, failing) } }
        runCurrent()
        gate.complete(Unit)

        assertTrue(first.await().exceptionOrNull() is IllegalStateException)
        assertTrue(second.await().exceptionOrNull() is IllegalStateException)

        assertEquals("art", loads.load(1) { executions++; "art" })
        assertEquals(2, executions)
    }
}
