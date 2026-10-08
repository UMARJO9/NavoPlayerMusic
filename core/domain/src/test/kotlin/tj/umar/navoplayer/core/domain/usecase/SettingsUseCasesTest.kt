package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

class SettingsUseCasesTest {

    private val repository = FakeSettingsRepository()

    @Test
    fun `setters write through`() = runTest {
        assertEquals(NavoResult.Success(Unit), SetMinTrackDurationUseCase(repository)(MinTrackDuration.TenSeconds))
        SetFolderExcludedUseCase(repository)("Recordings", true)
        SetPauseOnHeadphonesDisconnectUseCase(repository)(false)

        assertEquals(
            UserSettings(MinTrackDuration.TenSeconds, setOf("Recordings"), pauseOnHeadphonesDisconnect = false),
            ObserveSettingsUseCase(repository)().first(),
        )
    }

    @Test
    fun `write failure is reported`() = runTest {
        repository.writeError = IllegalStateException("disk full")

        assertTrue(SetMinTrackDurationUseCase(repository)(MinTrackDuration.TenSeconds) is NavoResult.Error)
        assertTrue(SetFolderExcludedUseCase(repository)("a", true) is NavoResult.Error)
        assertTrue(SetPauseOnHeadphonesDisconnectUseCase(repository)(true) is NavoResult.Error)
    }
}
