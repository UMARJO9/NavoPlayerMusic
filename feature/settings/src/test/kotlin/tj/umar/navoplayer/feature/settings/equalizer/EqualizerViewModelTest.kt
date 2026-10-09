package tj.umar.navoplayer.feature.settings.equalizer

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import tj.umar.navoplayer.core.domain.usecase.ObserveEqualizerUseCase
import tj.umar.navoplayer.core.domain.usecase.ResetEqualizerUseCase
import tj.umar.navoplayer.core.domain.usecase.SelectEqualizerPresetUseCase
import tj.umar.navoplayer.core.domain.usecase.SetBassBoostStrengthUseCase
import tj.umar.navoplayer.core.domain.usecase.SetEqualizerBandLevelUseCase
import tj.umar.navoplayer.core.domain.usecase.SetEqualizerEnabledUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.testEqualizerCapabilities
import tj.umar.navoplayer.core.testing.playback.FakeEqualizerController
import tj.umar.navoplayer.core.testing.repository.FakeEqualizerSettingsRepository

class EqualizerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakeEqualizerController()
    private val repository = FakeEqualizerSettingsRepository()
    private val observe = ObserveEqualizerUseCase(controller, repository)
    private val viewModel = EqualizerViewModel(
        observeEqualizer = observe,
        setEqualizerEnabled = SetEqualizerEnabledUseCase(repository),
        selectEqualizerPreset = SelectEqualizerPresetUseCase(repository),
        setEqualizerBandLevel = SetEqualizerBandLevelUseCase(observe, repository),
        setBassBoostStrength = SetBassBoostStrengthUseCase(repository),
        resetEqualizer = ResetEqualizerUseCase(repository),
    )

    private fun startReady(enabled: Boolean = true) {
        repository.emit(EqualizerSettings(enabled = enabled))
        controller.availability.value = EqualizerAvailability.Supported(testEqualizerCapabilities)
        viewModel.onIntent(EqualizerIntent.ScreenStarted)
    }

    @Test
    fun `starts loading then ready`() {
        viewModel.onIntent(EqualizerIntent.ScreenStarted)
        assertEquals(EqualizerPhase.Loading, viewModel.state.value.phase)

        controller.availability.value = EqualizerAvailability.Supported(testEqualizerCapabilities)

        val state = viewModel.state.value
        assertEquals(EqualizerPhase.Ready, state.phase)
        assertEquals(5, state.bands.size)
        assertEquals(3, state.presets.size)
        assertTrue(state.bassBoostSupported)
    }

    @Test
    fun `unsupported device shows unsupported`() {
        controller.availability.value = EqualizerAvailability.Unsupported

        viewModel.onIntent(EqualizerIntent.ScreenStarted)

        assertEquals(EqualizerPhase.Unsupported, viewModel.state.value.phase)
    }

    @Test
    fun `load failure can be retried`() {
        repository.observeError = IllegalStateException("disk")
        controller.availability.value = EqualizerAvailability.Supported(testEqualizerCapabilities)
        viewModel.onIntent(EqualizerIntent.ScreenStarted)
        assertEquals(EqualizerPhase.LoadFailed, viewModel.state.value.phase)

        repository.observeError = null
        viewModel.onIntent(EqualizerIntent.RetryLoad)

        assertEquals(EqualizerPhase.Ready, viewModel.state.value.phase)
    }

    @Test
    fun `screen stopped unsubscribes`() {
        startReady()
        assertEquals(1, controller.subscribers)

        viewModel.onIntent(EqualizerIntent.ScreenStopped)

        assertEquals(0, controller.subscribers)
    }

    @Test
    fun `toggle enables equalizer`() {
        startReady(enabled = false)

        viewModel.onIntent(EqualizerIntent.EnabledToggled(true))

        assertTrue(repository.current.enabled)
        assertTrue(viewModel.state.value.controlsEnabled)
    }

    @Test
    fun `preset and custom selection are saved`() {
        startReady()

        viewModel.onIntent(EqualizerIntent.PresetSelected(2))
        assertEquals(EqualizerPresetSelection.Preset(2), repository.current.preset)
        assertEquals(listOf(500, 300, -100, 300, 500), viewModel.state.value.bands.map { it.levelMb })

        viewModel.onIntent(EqualizerIntent.CustomSelected)
        assertEquals(EqualizerPresetSelection.Custom, repository.current.preset)
    }

    @Test
    fun `band drag writes custom levels and keeps draft`() {
        startReady()
        viewModel.onIntent(EqualizerIntent.PresetSelected(2))

        viewModel.onIntent(EqualizerIntent.BandLevelChanged(1, 800))

        val state = viewModel.state.value
        assertEquals(1, state.draggingBand)
        assertEquals(800, state.bands[1].levelMb)
        assertEquals(EqualizerPresetSelection.Custom, state.selectedPreset)
        assertEquals(listOf(500, 800, -100, 300, 500), repository.current.customBandLevelsMb)

        viewModel.onIntent(EqualizerIntent.BandLevelChangeFinished(1))
        assertNull(viewModel.state.value.draggingBand)
    }

    @Test
    fun `incoming status keeps dragged band draft`() {
        startReady()
        viewModel.onIntent(EqualizerIntent.BandLevelChanged(0, 400))

        repository.emit(repository.current.copy(bassBoostStrength = 100))

        assertEquals(400, viewModel.state.value.bands[0].levelMb)
    }

    @Test
    fun `bass boost drag is saved`() {
        startReady()

        viewModel.onIntent(EqualizerIntent.BassBoostChanged(600))
        assertTrue(viewModel.state.value.draggingBassBoost)
        viewModel.onIntent(EqualizerIntent.BassBoostChangeFinished)

        assertEquals(600, repository.current.bassBoostStrength)
        assertFalse(viewModel.state.value.draggingBassBoost)
    }

    @Test
    fun `reset restores flat curve`() {
        startReady()
        viewModel.onIntent(EqualizerIntent.BandLevelChanged(0, 400))

        viewModel.onIntent(EqualizerIntent.ResetClicked)

        assertEquals(EqualizerSettings(enabled = true), repository.current)
    }

    @Test
    fun `controls are ignored when disabled`() {
        startReady(enabled = false)

        viewModel.onIntent(EqualizerIntent.PresetSelected(1))
        viewModel.onIntent(EqualizerIntent.BandLevelChanged(0, 400))
        viewModel.onIntent(EqualizerIntent.BassBoostChanged(500))
        viewModel.onIntent(EqualizerIntent.ResetClicked)

        assertEquals(0, repository.writeCalls)
    }

    @Test
    fun `write failure shows message`() = runTest {
        startReady()
        repository.writeError = IllegalStateException("disk")

        viewModel.effects.test {
            viewModel.onIntent(EqualizerIntent.EnabledToggled(false))
            assertEquals(EqualizerEffect.ShowSaveFailed, awaitItem())
        }
    }

    @Test
    fun `back navigates back`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(EqualizerIntent.BackClicked)
            assertEquals(EqualizerEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `final level of one band survives a change to another band`() {
        startReady()
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate

        viewModel.onIntent(EqualizerIntent.BandLevelChanged(0, 200))
        viewModel.onIntent(EqualizerIntent.BandLevelChanged(1, 300))
        viewModel.onIntent(EqualizerIntent.BandLevelChanged(1, 400))
        viewModel.onIntent(EqualizerIntent.BandLevelChanged(2, 500))
        repository.writeGate = null
        gate.complete(Unit)

        assertEquals(listOf(200, 400, 500, 0, 0), repository.current.customBandLevelsMb)
    }

    @Test
    fun `reset after drag is not undone by pending draft`() {
        startReady()
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate

        viewModel.onIntent(EqualizerIntent.BandLevelChanged(0, 600))
        viewModel.onIntent(EqualizerIntent.BandLevelChangeFinished(0))
        viewModel.onIntent(EqualizerIntent.ResetClicked)
        repository.writeGate = null
        gate.complete(Unit)

        assertEquals(EqualizerSettings(enabled = true), repository.current)
    }

    @Test
    fun `stopping clears drag state`() {
        startReady()
        viewModel.onIntent(EqualizerIntent.BandLevelChanged(0, 600))

        viewModel.onIntent(EqualizerIntent.ScreenStopped)

        assertNull(viewModel.state.value.draggingBand)
    }
}
