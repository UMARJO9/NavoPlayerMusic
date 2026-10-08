package tj.umar.navoplayer.core.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import tj.umar.navoplayer.core.datastore.settings.SettingsPreferencesDataSource
import tj.umar.navoplayer.core.datastore.settings.StoredSettings
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import java.io.File

class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.repository() = DataStoreSettingsRepository(
        SettingsPreferencesDataSource(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(tmp.root, "settings.preferences_pb") },
            ),
        ),
    )

    @Test
    fun `settings round trip`() = runTest {
        val repository = repository()

        repository.setMinTrackDuration(MinTrackDuration.SixtySeconds)
        repository.setFolderExcluded("Recordings", true)
        repository.setPauseOnHeadphonesDisconnect(false)

        assertEquals(
            UserSettings(MinTrackDuration.SixtySeconds, setOf("Recordings"), pauseOnHeadphonesDisconnect = false),
            repository.observeSettings().first(),
        )
    }

    @Test
    fun `same value does not emit again`() = runTest {
        val repository = repository()

        repository.observeSettings().test {
            assertEquals(UserSettings(), awaitItem())
            repository.setFolderExcluded("Missing", false)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unknown stored duration maps to off`() {
        assertEquals(MinTrackDuration.Off, StoredSettings(7, emptySet(), true).toUserSettings().minTrackDuration)
    }
}
