package tj.umar.navoplayer.feature.welcome.welcome

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.testing.MainDispatcherRule

class WelcomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedStateHandle = SavedStateHandle()
    private val viewModel = WelcomeViewModel(savedStateHandle)

    private val dismissedResult =
        WelcomeIntent.PermissionResult(granted = false, rationaleBefore = false, rationaleAfter = false)

    @Test
    fun `initial state is not requested and sends nothing`() = runTest {
        assertEquals(WelcomePermissionStatus.NotRequested, viewModel.state.value.permission)
        viewModel.effects.test { expectNoEvents() }
    }

    @Test
    fun `started with permission navigates to library`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(WelcomeIntent.ScreenStarted(granted = true))
            assertEquals(WelcomeEffect.NavigateToLibrary, awaitItem())
        }
    }

    @Test
    fun `started without permission does nothing`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(WelcomeIntent.ScreenStarted(granted = false))
            expectNoEvents()
        }
    }

    @Test
    fun `grant click requests permission`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(WelcomeIntent.GrantAccessClicked)
            assertEquals(WelcomeEffect.RequestAudioPermission, awaitItem())
        }
    }

    @Test
    fun `grant click when permanently denied opens settings`() = runTest {
        viewModel.onIntent(WelcomeIntent.PermissionResult(granted = false, rationaleBefore = true, rationaleAfter = false))

        viewModel.effects.test {
            viewModel.onIntent(WelcomeIntent.GrantAccessClicked)
            assertEquals(WelcomeEffect.OpenAppSettings, awaitItem())
        }
    }

    @Test
    fun `granted result navigates to library`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(WelcomeIntent.PermissionResult(granted = true, rationaleBefore = false, rationaleAfter = false))
            assertEquals(WelcomeEffect.NavigateToLibrary, awaitItem())
        }
    }

    @Test
    fun `dismissed dialog is denied`() {
        viewModel.onIntent(dismissedResult)

        assertEquals(WelcomePermissionStatus.Denied, viewModel.state.value.permission)
    }

    @Test
    fun `second silent denial is permanently denied`() {
        viewModel.onIntent(dismissedResult)
        viewModel.onIntent(dismissedResult)

        assertEquals(WelcomePermissionStatus.PermanentlyDenied, viewModel.state.value.permission)
    }

    @Test
    fun `denial with rationale resets silent denial`() {
        viewModel.onIntent(dismissedResult)
        viewModel.onIntent(WelcomeIntent.PermissionResult(granted = false, rationaleBefore = false, rationaleAfter = true))
        viewModel.onIntent(dismissedResult)

        assertEquals(WelcomePermissionStatus.Denied, viewModel.state.value.permission)
    }

    @Test
    fun `denial after rationale is permanently denied`() {
        viewModel.onIntent(WelcomeIntent.PermissionResult(granted = false, rationaleBefore = true, rationaleAfter = false))

        assertEquals(WelcomePermissionStatus.PermanentlyDenied, viewModel.state.value.permission)
    }

    @Test
    fun `permanently denied survives restore`() {
        viewModel.onIntent(WelcomeIntent.PermissionResult(granted = false, rationaleBefore = true, rationaleAfter = false))

        val restored = WelcomeViewModel(savedStateHandle)

        assertEquals(WelcomePermissionStatus.PermanentlyDenied, restored.state.value.permission)
    }

    @Test
    fun `silent denial survives restore`() {
        viewModel.onIntent(dismissedResult)
        val restored = WelcomeViewModel(savedStateHandle)
        restored.onIntent(dismissedResult)

        assertEquals(WelcomePermissionStatus.PermanentlyDenied, restored.state.value.permission)
    }
}
