package tj.umar.navoplayer.feature.library.library

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.testing.MainDispatcherRule

class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state selects tracks`() {
        val state = LibraryViewModel().state.value
        assertEquals(LibraryTab.Tracks, state.selectedTab)
    }

    @Test
    fun `TabSelected updates selected tab`() = runTest {
        val viewModel = LibraryViewModel()

        viewModel.state.test {
            assertEquals(LibraryTab.Tracks, awaitItem().selectedTab)
            viewModel.onIntent(LibraryIntent.TabSelected(LibraryTab.Albums))
            assertEquals(LibraryTab.Albums, awaitItem().selectedTab)
        }
    }

    @Test
    fun `TabSelected with current tab emits nothing`() = runTest {
        val viewModel = LibraryViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(LibraryIntent.TabSelected(LibraryTab.Tracks))
            expectNoEvents()
        }
    }
}
