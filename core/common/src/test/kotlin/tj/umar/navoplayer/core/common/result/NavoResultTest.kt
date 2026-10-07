package tj.umar.navoplayer.core.common.result

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class NavoResultTest {

    private val error = IllegalStateException("boom")

    @Test
    fun `map transforms success`() {
        val result = NavoResult.Success(2).map { it * 10 }
        assertEquals(NavoResult.Success(20), result)
    }

    @Test
    fun `map passes error through`() {
        val result: NavoResult<Int> = NavoResult.Error(error)
        assertEquals(NavoResult.Error(error), result.map { it * 10 })
    }

    @Test
    fun `onSuccess runs only for success`() {
        var successCalled = false
        var errorCalled = false
        NavoResult.Success(1)
            .onSuccess { successCalled = true }
            .onError { errorCalled = true }
        assertTrue(successCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `onError runs only for error`() {
        var successCalled = false
        var received: Throwable? = null
        NavoResult.Error(error)
            .onSuccess { successCalled = true }
            .onError { received = it }
        assertFalse(successCalled)
        assertSame(error, received)
    }

    @Test
    fun `navoRunCatching wraps value in success`() = runTest {
        assertEquals(NavoResult.Success("ok"), navoRunCatching { "ok" })
    }

    @Test
    fun `navoRunCatching wraps exception in error`() = runTest {
        assertEquals(NavoResult.Error(error), navoRunCatching { throw error })
    }

    @Test(expected = CancellationException::class)
    fun `navoRunCatching rethrows cancellation`() = runTest {
        navoRunCatching { throw CancellationException("cancelled") }
    }
}
