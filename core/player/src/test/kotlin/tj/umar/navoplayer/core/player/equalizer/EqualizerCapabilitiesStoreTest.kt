package tj.umar.navoplayer.core.player.equalizer

import app.cash.turbine.test
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.model.EqualizerCapabilities
import tj.umar.navoplayer.core.testing.data.testEqualizerCapabilities

class EqualizerCapabilitiesStoreTest {

    private var probeCalls = 0
    private var probeResult: EqualizerCapabilities? = testEqualizerCapabilities

    private fun TestScope.store() = EqualizerCapabilitiesStore(
        probe = {
            probeCalls++
            probeResult
        },
        mainDispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun `probes once for concurrent callers`() = runTest {
        val store = store()

        List(3) { async { store.ensureProbed() } }.awaitAll()
        store.ensureProbed()

        assertEquals(1, probeCalls)
        assertEquals(EqualizerAvailability.Supported(testEqualizerCapabilities), store.availability.value)
    }

    @Test
    fun `failed probe is unsupported`() = runTest {
        probeResult = null
        val store = store()

        store.ensureProbed()

        assertEquals(EqualizerAvailability.Unsupported, store.availability.value)
    }

    @Test
    fun `publish and mark unsupported override probe`() = runTest {
        val store = store()

        store.publish(testEqualizerCapabilities)
        assertEquals(EqualizerAvailability.Supported(testEqualizerCapabilities), store.availability.value)

        store.markUnsupported()
        assertEquals(EqualizerAvailability.Unsupported, store.availability.value)
    }

    @Test
    fun `controller probes on subscribe`() = runTest {
        val controller = DefaultEqualizerController(store())

        controller.observeAvailability().test {
            assertEquals(EqualizerAvailability.Supported(testEqualizerCapabilities), awaitItem())
        }
        assertEquals(1, probeCalls)
    }
}
