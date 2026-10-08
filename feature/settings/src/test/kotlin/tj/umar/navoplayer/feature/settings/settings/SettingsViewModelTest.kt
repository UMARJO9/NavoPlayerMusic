package tj.umar.navoplayer.feature.settings.settings

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.GetAppInfoUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveSettingsUseCase
import tj.umar.navoplayer.core.domain.usecase.SetMinTrackDurationUseCase
import tj.umar.navoplayer.core.domain.usecase.SetPauseOnHeadphonesDisconnectUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.app.FakeAppInfoProvider
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository(
        UserSettings(excludedFolders = setOf("A", "B"), pauseOnHeadphonesDisconnect = false),
    )

    private fun viewModel() = SettingsViewModel(
        observeSettings = ObserveSettingsUseCase(settings),
        getAppInfo = GetAppInfoUseCase(FakeAppInfoProvider("2.1")),
        setMinTrackDuration = SetMinTrackDurationUseCase(settings),
        setPauseOnHeadphonesDisconnect = SetPauseOnHeadphonesDisconnectUseCase(settings),
    )

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
            viewModel.onIntent(SettingsIntent.BackClicked)
            assertEquals(SettingsEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `settings error falls back to defaults`() {
        settings.observeError = IllegalStateException("broken")

        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertTrue(state.pauseOnHeadphonesDisconnect)
    }
}
