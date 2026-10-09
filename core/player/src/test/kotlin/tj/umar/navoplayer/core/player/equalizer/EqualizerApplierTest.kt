package tj.umar.navoplayer.core.player.equalizer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.equalizer.resolve
import tj.umar.navoplayer.core.domain.model.EqualizerProfile
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import tj.umar.navoplayer.core.domain.model.EqualizerStatus
import tj.umar.navoplayer.core.testing.data.testEqualizerCapabilities

private class FakeSoundEffects(val sessionId: Int, val onControlRegained: () -> Unit) : SoundEffects {
    val applied = mutableListOf<EqualizerProfile>()
    var released = false
    var applyError: RuntimeException? = null

    override fun apply(profile: EqualizerProfile) {
        applyError?.let { throw it }
        applied += profile
    }

    override fun release() {
        released = true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EqualizerApplierTest {

    private val created = mutableListOf<FakeSoundEffects>()
    private var createError: RuntimeException? = null
    private val factory = SoundEffectsFactory { sessionId, _, onControlRegained ->
        createError?.let { throw it }
        FakeSoundEffects(sessionId, onControlRegained).also { created += it }
    }
    private val status = MutableStateFlow<EqualizerStatus>(EqualizerStatus.Probing)
    private val sessionIds = MutableStateFlow(7)

    private fun ready(settings: EqualizerSettings): EqualizerStatus.Ready =
        EqualizerStatus.Ready(testEqualizerCapabilities, settings, settings.resolve(testEqualizerCapabilities))

    private fun TestScope.start(): EqualizerApplier {
        val applier = EqualizerApplier(status, sessionIds, factory)
        applier.start(backgroundScope)
        runCurrent()
        return applier
    }

    @Test
    fun `nothing is created while probing or disabled`() = runTest {
        start()
        status.value = ready(EqualizerSettings(enabled = false))
        runCurrent()

        assertTrue(created.isEmpty())
    }

    @Test
    fun `enabled profile is applied on current session`() = runTest {
        start()
        val enabled = ready(EqualizerSettings(enabled = true, customBandLevelsMb = listOf(300)))

        status.value = enabled
        runCurrent()

        assertEquals(7, created.single().sessionId)
        assertEquals(listOf(enabled.profile), created.single().applied)
    }

    @Test
    fun `profile change reapplies without recreating`() = runTest {
        start()
        status.value = ready(EqualizerSettings(enabled = true))
        runCurrent()

        status.value = ready(EqualizerSettings(enabled = true, customBandLevelsMb = listOf(500)))
        runCurrent()

        assertEquals(1, created.size)
        assertEquals(2, created.single().applied.size)
    }

    @Test
    fun `turning off releases effects`() = runTest {
        start()
        status.value = ready(EqualizerSettings(enabled = true))
        runCurrent()

        status.value = ready(EqualizerSettings(enabled = false))
        runCurrent()

        assertTrue(created.single().released)
    }

    @Test
    fun `new session recreates effects`() = runTest {
        start()
        status.value = ready(EqualizerSettings(enabled = true))
        runCurrent()

        sessionIds.value = 9
        runCurrent()

        assertTrue(created[0].released)
        assertEquals(9, created[1].sessionId)
    }

    @Test
    fun `create failure is retried on next change`() = runTest {
        start()
        createError = UnsupportedOperationException("engine busy")

        status.value = ready(EqualizerSettings(enabled = true))
        runCurrent()
        assertTrue(created.isEmpty())

        createError = null
        status.value = ready(EqualizerSettings(enabled = true, customBandLevelsMb = listOf(100)))
        runCurrent()

        assertEquals(1, created.size)
        assertEquals(listOf(100, 0, 0, 0, 0), created.single().applied.single().bandLevelsMb)
    }

    @Test
    fun `apply failure is ignored and control regain reapplies`() = runTest {
        start()
        status.value = ready(EqualizerSettings(enabled = true))
        runCurrent()
        val effects = created.single()
        effects.applyError = IllegalStateException("lost control")

        status.value = ready(EqualizerSettings(enabled = true, customBandLevelsMb = listOf(200)))
        runCurrent()
        effects.applyError = null
        effects.onControlRegained()

        assertEquals(listOf(200, 0, 0, 0, 0), effects.applied.last().bandLevelsMb)
    }

    @Test
    fun `release frees effects and stops`() = runTest {
        val applier = start()
        status.value = ready(EqualizerSettings(enabled = true))
        runCurrent()

        applier.release()
        status.value = ready(EqualizerSettings(enabled = true, customBandLevelsMb = listOf(100)))
        runCurrent()

        assertTrue(created.single().released)
        assertEquals(1, created.size)
    }
}
