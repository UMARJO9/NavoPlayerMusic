package tj.umar.navoplayer.feature.library.library

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTrackRepository()
    private val savedStateHandle = SavedStateHandle()
    private val viewModel = LibraryViewModel(ObserveTracksUseCase(repository), savedStateHandle)

    @Test
    fun `initial state selects tracks and does not load`() {
        val state = viewModel.state.value

        assertEquals(LibraryTab.Tracks, state.selectedTab)
        assertEquals(AudioPermissionStatus.Unknown, state.audioPermission)
        assertFalse(state.isLoadingTracks)
        assertEquals(0, repository.observeCalls)
    }

    @Test
    fun `TabSelected updates selected tab`() = runTest {
        viewModel.state.test {
            assertEquals(LibraryTab.Tracks, awaitItem().selectedTab)
            viewModel.onIntent(LibraryIntent.TabSelected(LibraryTab.Albums))
            assertEquals(LibraryTab.Albums, awaitItem().selectedTab)
        }
    }

    @Test
    fun `TabSelected with current tab emits nothing`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(LibraryIntent.TabSelected(LibraryTab.Tracks))
            expectNoEvents()
        }
    }

    @Test
    fun `granted check starts loading and then shows tracks`() = runTest {
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))

        val loading = viewModel.state.value
        assertEquals(AudioPermissionStatus.Granted, loading.audioPermission)
        assertTrue(loading.isLoadingTracks)

        repository.emit(TestTracks.tracks)

        val loaded = viewModel.state.value
        assertFalse(loaded.isLoadingTracks)
        assertEquals(TestTracks.tracks, loaded.tracks)
    }

    @Test
    fun `repeated granted checks observe tracks once`() {
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))

        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `first denied check requests permission`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.PermissionChecked(granted = false))

            assertEquals(LibraryEffect.RequestAudioPermission, awaitItem())
            assertEquals(AudioPermissionStatus.Denied, viewModel.state.value.audioPermission)
        }
    }

    @Test
    fun `later denied checks do not request again`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.PermissionChecked(granted = false))
            awaitItem()
            viewModel.onIntent(LibraryIntent.PermissionChecked(granted = false))
            expectNoEvents()
        }
    }

    @Test
    fun `denied check keeps permanently denied status`() {
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = false, rationaleBefore = true, rationaleAfter = false))
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = false))

        assertEquals(AudioPermissionStatus.PermanentlyDenied, viewModel.state.value.audioPermission)
    }

    @Test
    fun `granted result starts observing tracks`() {
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = true, rationaleBefore = false, rationaleAfter = false))

        assertEquals(AudioPermissionStatus.Granted, viewModel.state.value.audioPermission)
        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `denial with rationale is denied`() {
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = false, rationaleBefore = false, rationaleAfter = true))

        assertEquals(AudioPermissionStatus.Denied, viewModel.state.value.audioPermission)
        assertEquals(0, repository.observeCalls)
    }

    @Test
    fun `denial after rationale is permanently denied`() {
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = false, rationaleBefore = true, rationaleAfter = false))

        assertEquals(AudioPermissionStatus.PermanentlyDenied, viewModel.state.value.audioPermission)
    }

    @Test
    fun `grant click when denied requests permission`() = runTest {
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = false, rationaleBefore = false, rationaleAfter = true))

        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.GrantPermissionClicked)
            assertEquals(LibraryEffect.RequestAudioPermission, awaitItem())
        }
    }

    @Test
    fun `grant click when permanently denied opens settings`() = runTest {
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = false, rationaleBefore = true, rationaleAfter = false))

        viewModel.effects.test {
            viewModel.onIntent(LibraryIntent.GrantPermissionClicked)
            assertEquals(LibraryEffect.OpenAppSettings, awaitItem())
        }
    }

    @Test
    fun `empty library stops loading with no tracks`() = runTest {
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))
        repository.emit(emptyList())

        val state = viewModel.state.value
        assertFalse(state.isLoadingTracks)
        assertTrue(state.tracks.isEmpty())
    }

    @Test
    fun `repository error marks load as failed`() {
        repository.error = IllegalStateException("scan failed")

        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))

        val state = viewModel.state.value
        assertFalse(state.isLoadingTracks)
        assertTrue(state.tracksLoadFailed)
    }

    @Test
    fun `later emission updates tracks`() = runTest {
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))
        repository.emit(listOf(TestTracks.alpha))
        repository.emit(TestTracks.tracks)

        assertEquals(TestTracks.tracks, viewModel.state.value.tracks)
    }

    @Test
    fun `dismissed dialog without rationale stays denied`() {
        viewModel.onIntent(dismissedResult)

        assertEquals(AudioPermissionStatus.Denied, viewModel.state.value.audioPermission)
    }

    @Test
    fun `second silent denial is permanently denied`() {
        viewModel.onIntent(dismissedResult)
        viewModel.onIntent(dismissedResult)

        assertEquals(AudioPermissionStatus.PermanentlyDenied, viewModel.state.value.audioPermission)
    }

    @Test
    fun `denial with rationale resets silent denial count`() {
        viewModel.onIntent(dismissedResult)
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = false, rationaleBefore = false, rationaleAfter = true))
        viewModel.onIntent(dismissedResult)

        assertEquals(AudioPermissionStatus.Denied, viewModel.state.value.audioPermission)
    }

    @Test
    fun `restored denied status does not request permission again`() = runTest {
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = false))
        val restored = LibraryViewModel(ObserveTracksUseCase(repository), savedStateHandle)

        restored.effects.test {
            restored.onIntent(LibraryIntent.PermissionChecked(granted = false))
            expectNoEvents()
        }
        assertEquals(AudioPermissionStatus.Denied, restored.state.value.audioPermission)
    }

    @Test
    fun `restored permanently denied status opens settings`() = runTest {
        viewModel.onIntent(LibraryIntent.PermissionResult(granted = false, rationaleBefore = true, rationaleAfter = false))
        val restored = LibraryViewModel(ObserveTracksUseCase(repository), savedStateHandle)
        restored.onIntent(LibraryIntent.PermissionChecked(granted = false))

        assertEquals(AudioPermissionStatus.PermanentlyDenied, restored.state.value.audioPermission)
        restored.effects.test {
            restored.onIntent(LibraryIntent.GrantPermissionClicked)
            assertEquals(LibraryEffect.OpenAppSettings, awaitItem())
        }
    }

    @Test
    fun `silent denial survives restore`() {
        viewModel.onIntent(dismissedResult)
        val restored = LibraryViewModel(ObserveTracksUseCase(repository), savedStateHandle)
        restored.onIntent(dismissedResult)

        assertEquals(AudioPermissionStatus.PermanentlyDenied, restored.state.value.audioPermission)
    }

    @Test
    fun `retry after failure observes tracks again`() = runTest {
        repository.error = IllegalStateException("scan failed")
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))
        repository.error = null

        viewModel.onIntent(LibraryIntent.RetryLoadTracks)
        repository.emit(TestTracks.tracks)

        val state = viewModel.state.value
        assertEquals(2, repository.observeCalls)
        assertFalse(state.tracksLoadFailed)
        assertEquals(TestTracks.tracks, state.tracks)
    }

    @Test
    fun `retry while observing does not subscribe twice`() {
        viewModel.onIntent(LibraryIntent.PermissionChecked(granted = true))
        viewModel.onIntent(LibraryIntent.RetryLoadTracks)

        assertEquals(1, repository.observeCalls)
    }

    private val dismissedResult =
        LibraryIntent.PermissionResult(granted = false, rationaleBefore = false, rationaleAfter = false)
}
