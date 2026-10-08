package tj.umar.navoplayer.feature.settings.folders

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.ObserveAllFoldersUseCase
import tj.umar.navoplayer.core.domain.usecase.SetFolderExcludedUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class HiddenFoldersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracks = FakeTrackRepository()
    private val settings = FakeSettingsRepository(UserSettings(excludedFolders = setOf("Music/Navo")))

    private val viewModel = HiddenFoldersViewModel(
        observeAllFolders = ObserveAllFoldersUseCase(tracks, settings, mainDispatcherRule.testDispatcher),
        setFolderExcluded = SetFolderExcludedUseCase(settings),
    )

    private suspend fun started() {
        viewModel.onIntent(HiddenFoldersIntent.ScreenStarted(hasPermission = true))
        tracks.emit(TestTracks.tracks)
    }

    @Test
    fun `nothing is observed before start`() {
        assertEquals(0, tracks.observeCalls)
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `missing permission navigates to welcome`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(HiddenFoldersIntent.ScreenStarted(hasPermission = false))
            assertEquals(HiddenFoldersEffect.NavigateToWelcome, awaitItem())
        }
    }

    @Test
    fun `loads folders with exclusion flags`() = runTest {
        started()

        val folders = viewModel.state.value.folders
        assertEquals(listOf("Music/Mixes", "Music/Navo"), folders.map { it.path })
        assertEquals(listOf(false, true), folders.map { it.isExcluded })
    }

    @Test
    fun `toggling visibility writes exclusion`() = runTest {
        started()

        viewModel.onIntent(HiddenFoldersIntent.FolderVisibilityToggled("Music/Navo", visible = true))
        viewModel.onIntent(HiddenFoldersIntent.FolderVisibilityToggled("Music/Mixes", visible = false))

        assertEquals(setOf("Music/Mixes"), settings.current.excludedFolders)
        assertEquals(listOf(true, false), viewModel.state.value.folders.map { it.isExcluded })
    }

    @Test
    fun `write error shows message`() = runTest {
        started()
        settings.writeError = IllegalStateException("disk full")

        viewModel.effects.test {
            viewModel.onIntent(HiddenFoldersIntent.FolderVisibilityToggled("Music/Navo", visible = true))
            assertEquals(HiddenFoldersEffect.ShowSaveFailed, awaitItem())
        }
    }

    @Test
    fun `load error can be retried`() = runTest {
        tracks.error = IllegalStateException("scan failed")
        started()
        assertTrue(viewModel.state.value.loadFailed)

        tracks.error = null
        viewModel.onIntent(HiddenFoldersIntent.RetryLoad)

        assertFalse(viewModel.state.value.loadFailed)
        assertEquals(2, viewModel.state.value.folders.size)
    }

    @Test
    fun `restart resubscribes without loading`() = runTest {
        started()

        viewModel.onIntent(HiddenFoldersIntent.ScreenStopped)
        viewModel.onIntent(HiddenFoldersIntent.ScreenStarted(hasPermission = true))

        assertEquals(2, tracks.observeCalls)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `back navigates back`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(HiddenFoldersIntent.BackClicked)
            assertEquals(HiddenFoldersEffect.NavigateBack, awaitItem())
        }
    }
}
