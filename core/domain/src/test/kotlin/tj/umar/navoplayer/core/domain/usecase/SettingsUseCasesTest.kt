package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSort
import tj.umar.navoplayer.core.domain.model.TrackSortField
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.testing.app.FakeAppInfoProvider
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

    @Test
    fun `app info comes from provider`() {
        assertEquals("2.3", GetAppInfoUseCase(FakeAppInfoProvider("2.3"))().versionName)
    }

    @Test
    fun `sort setters write field and direction separately`() = runTest {
        SetTrackSortUseCase(repository)(TrackSortField.Duration)
        SetTrackSortUseCase(repository)(SortDirection.Descending)
        SetGroupSortUseCase(repository)(GroupSortField.TrackCount)

        assertEquals(TrackSort(TrackSortField.Duration, SortDirection.Descending), repository.current.trackSort)
        assertEquals(GroupSort(GroupSortField.TrackCount, SortDirection.Ascending), repository.current.groupSort)

        repository.writeError = IllegalStateException("disk full")
        assertTrue(SetGroupSortUseCase(repository)(SortDirection.Descending) is NavoResult.Error)
    }
}
