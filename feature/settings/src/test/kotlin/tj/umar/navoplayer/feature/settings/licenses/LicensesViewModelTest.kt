package tj.umar.navoplayer.feature.settings.licenses

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.testing.MainDispatcherRule

class LicensesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = LicensesViewModel()

    @Test
    fun `entries are listed once each`() {
        val entries = viewModel.state.value.entries

        assertTrue(entries.isNotEmpty())
        assertEquals(entries.size, entries.map { it.nameRes }.toSet().size)
    }

    @Test
    fun `back navigates back`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LicensesIntent.BackClicked)
            assertEquals(LicensesEffect.NavigateBack, awaitItem())
        }
    }
}
