package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

class ThemeModeUseCasesTest {

    private val repository = FakeSettingsRepository()

    @Test
    fun `observe emits current mode then only changes`() = runTest {
        ObserveThemeModeUseCase(repository)().test {
            assertEquals(ThemeMode.Dark, awaitItem())

            SetMinTrackDurationUseCase(repository)(MinTrackDuration.TenSeconds)
            expectNoEvents()

            assertEquals(NavoResult.Success(Unit), SetThemeModeUseCase(repository)(ThemeMode.Light))
            assertEquals(ThemeMode.Light, awaitItem())

            SetThemeModeUseCase(repository)(ThemeMode.System)
            assertEquals(ThemeMode.System, awaitItem())
        }
    }

    @Test
    fun `set writes through`() = runTest {
        SetThemeModeUseCase(repository)(ThemeMode.System)

        assertEquals(ThemeMode.System, repository.current.themeMode)
    }

    @Test
    fun `write failure is reported`() = runTest {
        repository.writeError = IllegalStateException("disk full")

        assertTrue(SetThemeModeUseCase(repository)(ThemeMode.Light) is NavoResult.Error)
        assertEquals(ThemeMode.Dark, repository.current.themeMode)
    }
}
