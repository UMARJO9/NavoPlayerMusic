package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

class ShowCoverArtUseCasesTest {

    private val repository = FakeSettingsRepository()

    @Test
    fun `cover art is shown by default`() = runTest {
        ObserveShowCoverArtUseCase(repository)().test {
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `observe emits only changes`() = runTest {
        ObserveShowCoverArtUseCase(repository)().test {
            assertEquals(true, awaitItem())

            SetThemeModeUseCase(repository)(ThemeMode.Light)
            expectNoEvents()

            assertEquals(NavoResult.Success(Unit), SetShowCoverArtUseCase(repository)(false))
            assertEquals(false, awaitItem())

            SetShowCoverArtUseCase(repository)(true)
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `set writes through`() = runTest {
        SetShowCoverArtUseCase(repository)(false)

        assertEquals(false, repository.current.showCoverArt)
    }

    @Test
    fun `write failure is reported`() = runTest {
        repository.writeError = IllegalStateException("disk full")

        assertTrue(SetShowCoverArtUseCase(repository)(false) is NavoResult.Error)
        assertEquals(true, repository.current.showCoverArt)
    }
}
