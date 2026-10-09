package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import tj.umar.navoplayer.core.domain.model.EqualizerStatus
import tj.umar.navoplayer.core.testing.data.testEqualizerCapabilities
import tj.umar.navoplayer.core.testing.playback.FakeEqualizerController
import tj.umar.navoplayer.core.testing.repository.FakeEqualizerSettingsRepository

class EqualizerUseCasesTest {

    private val controller = FakeEqualizerController()
    private val repository = FakeEqualizerSettingsRepository()
    private val observe = ObserveEqualizerUseCase(controller, repository)
    private val setBandLevel = SetEqualizerBandLevelUseCase(observe, repository)

    private fun ready() {
        controller.availability.value = EqualizerAvailability.Supported(testEqualizerCapabilities)
    }

    @Test
    fun `observe moves from probing to ready`() = runTest {
        observe().test {
            assertEquals(EqualizerStatus.Probing, awaitItem())
            ready()
            val status = awaitItem() as EqualizerStatus.Ready
            assertEquals(listOf(0, 0, 0, 0, 0), status.profile.bandLevelsMb)
        }
    }

    @Test
    fun `observe reports unsupported with settings`() = runTest {
        controller.availability.value = EqualizerAvailability.Unsupported

        observe().test {
            assertEquals(EqualizerStatus.Unsupported(EqualizerSettings()), awaitItem())
        }
    }

    @Test
    fun `band change from preset copies preset curve into custom`() = runTest {
        ready()
        repository.emit(EqualizerSettings(enabled = true, preset = EqualizerPresetSelection.Preset(2)))

        assertEquals(NavoResult.Success(Unit), setBandLevel(1, 640))

        assertEquals(EqualizerPresetSelection.Custom, repository.current.preset)
        assertEquals(listOf(500, 600, -100, 300, 500), repository.current.customBandLevelsMb)
    }

    @Test
    fun `band change clamps level`() = runTest {
        ready()

        setBandLevel(4, 9_000)

        assertEquals(listOf(0, 0, 0, 0, 1500), repository.current.customBandLevelsMb)
    }

    @Test
    fun `band change fails when not ready or band unknown`() = runTest {
        assertTrue(setBandLevel(0, 100) is NavoResult.Error)
        ready()
        assertTrue(setBandLevel(7, 100) is NavoResult.Error)
        assertTrue(repository.customLevelWrites.isEmpty())
    }

    @Test
    fun `simple writes forward to repository`() = runTest {
        SetEqualizerEnabledUseCase(repository)(true)
        SelectEqualizerPresetUseCase(repository)(EqualizerPresetSelection.Preset(1))
        SetBassBoostStrengthUseCase(repository)(4_000)

        assertEquals(
            EqualizerSettings(enabled = true, preset = EqualizerPresetSelection.Preset(1), bassBoostStrength = 1000),
            repository.current,
        )

        ResetEqualizerUseCase(repository)()
        assertEquals(EqualizerSettings(enabled = true), repository.current)
    }

    @Test
    fun `write failure maps to error`() = runTest {
        repository.writeError = IllegalStateException("disk")

        assertTrue(SetEqualizerEnabledUseCase(repository)(true) is NavoResult.Error)
    }
}
