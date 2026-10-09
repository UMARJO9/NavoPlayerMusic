package tj.umar.navoplayer.feature.settings.settings

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.GetAppInfoUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveEqualizerUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveSettingsUseCase
import tj.umar.navoplayer.core.domain.usecase.SetMinTrackDurationUseCase
import tj.umar.navoplayer.core.domain.usecase.SetPauseOnHeadphonesDisconnectUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.app.FakeAppInfoProvider
import tj.umar.navoplayer.core.testing.data.testEqualizerCapabilities
import tj.umar.navoplayer.core.testing.playback.FakeEqualizerController
import tj.umar.navoplayer.core.testing.repository.FakeEqualizerSettingsRepository
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository(
        UserSettings(excludedFolders = setOf("A", "B"), pauseOnHeadphonesDisconnect = false),
    )

    private val equalizerController = FakeEqualizerController()
    private val equalizerSettings = FakeEqualizerSettingsRepository()

    private fun viewModel() = SettingsViewModel(
        observeSettings = ObserveSettingsUseCase(settings),
        getAppInfo = GetAppInfoUseCase(FakeAppInfoProvider("2.1")),
        setMinTrackDuration = SetMinTrackDurationUseCase(settings),
        setPauseOnHeadphonesDisconnect = SetPauseOnHeadphonesDisconnectUseCase(settings),
        observeEqualizer = ObserveEqualizerUseCase(equalizerController, equalizerSettings),
    ).apply { onIntent(SettingsIntent.ScreenStarted) }

    @Test
    fun `state comes from settings and app info`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertEquals("2.1", state.versionName)
        assertEquals(2, state.hiddenFolderCount)
        assertFalse(state.pauseOnHeadphonesDisconnect)
        assertEquals(MinTrackDuration.Off, state.minTrackDuration)
    }

    @Test
    fun `duration selection writes and same value is ignored`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.MinTrackDurationSelected(MinTrackDuration.ThirtySeconds))
        viewModel.onIntent(SettingsIntent.MinTrackDurationSelected(MinTrackDuration.ThirtySeconds))

        assertEquals(MinTrackDuration.ThirtySeconds, viewModel.state.value.minTrackDuration)
        assertEquals(1, settings.writeCalls)
    }

    @Test
    fun `headphones toggle writes`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.PauseOnHeadphonesDisconnectToggled(true))

        assertTrue(viewModel.state.value.pauseOnHeadphonesDisconnect)
    }

    @Test
    fun `write error shows message`() = runTest {
        val viewModel = viewModel()
        settings.writeError = IllegalStateException("disk full")

        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.PauseOnHeadphonesDisconnectToggled(true))
            assertEquals(SettingsEffect.ShowSaveFailed, awaitItem())
        }
    }

    @Test
    fun `navigation intents send effects`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.HiddenFoldersClicked)
            assertEquals(SettingsEffect.NavigateToHiddenFolders, awaitItem())
            viewModel.onIntent(SettingsIntent.LicensesClicked)
            assertEquals(SettingsEffect.NavigateToLicenses, awaitItem())
            viewModel.onIntent(SettingsIntent.EqualizerClicked)
            assertEquals(SettingsEffect.NavigateToEqualizer, awaitItem())
            viewModel.onIntent(SettingsIntent.BackClicked)
            assertEquals(SettingsEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `settings error shows retry and blocks writes`() {
        settings.observeError = IllegalStateException("broken")
        val viewModel = viewModel()
        assertTrue(viewModel.state.value.loadFailed)

        viewModel.onIntent(SettingsIntent.PauseOnHeadphonesDisconnectToggled(true))
        assertEquals(0, settings.writeCalls)

        settings.observeError = null
        viewModel.onIntent(SettingsIntent.RetryLoad)
        assertFalse(viewModel.state.value.loadFailed)
        assertFalse(viewModel.state.value.pauseOnHeadphonesDisconnect)
    }

    @Test
    fun `nothing is read before start and after stop`() {
        val viewModel = SettingsViewModel(
            observeSettings = ObserveSettingsUseCase(settings),
            getAppInfo = GetAppInfoUseCase(FakeAppInfoProvider()),
            setMinTrackDuration = SetMinTrackDurationUseCase(settings),
            setPauseOnHeadphonesDisconnect = SetPauseOnHeadphonesDisconnectUseCase(settings),
            observeEqualizer = ObserveEqualizerUseCase(equalizerController, equalizerSettings),
        )
        assertEquals(0, settings.observeCalls)

        viewModel.onIntent(SettingsIntent.ScreenStarted)
        viewModel.onIntent(SettingsIntent.ScreenStopped)
        settings.emit(UserSettings(excludedFolders = setOf("C")))

        assertEquals(2, viewModel.state.value.hiddenFolderCount)
        assertEquals(0, equalizerController.subscribers)
    }

    @Test
    fun `equalizer summary follows equalizer status`() {
        val viewModel = viewModel()
        viewModel.onIntent(SettingsIntent.ScreenStarted)
        assertEquals(EqualizerSummary.Loading, viewModel.state.value.equalizerSummary)

        equalizerController.availability.value = EqualizerAvailability.Supported(testEqualizerCapabilities)
        assertEquals(EqualizerSummary.Off, viewModel.state.value.equalizerSummary)

        equalizerSettings.emit(EqualizerSettings(enabled = true))
        assertEquals(EqualizerSummary.Custom, viewModel.state.value.equalizerSummary)

        equalizerSettings.emit(EqualizerSettings(enabled = true, preset = EqualizerPresetSelection.Preset(2)))
        assertEquals(EqualizerSummary.Preset("Rock"), viewModel.state.value.equalizerSummary)

        equalizerController.availability.value = EqualizerAvailability.Unsupported
        assertEquals(EqualizerSummary.Unsupported, viewModel.state.value.equalizerSummary)
    }

    @Test
    fun `equalizer error does not fail settings`() {
        equalizerSettings.observeError = IllegalStateException("disk")
        equalizerController.availability.value = EqualizerAvailability.Supported(testEqualizerCapabilities)
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.ScreenStarted)

        assertEquals(EqualizerSummary.Off, viewModel.state.value.equalizerSummary)
        assertFalse(viewModel.state.value.loadFailed)
    }
}
