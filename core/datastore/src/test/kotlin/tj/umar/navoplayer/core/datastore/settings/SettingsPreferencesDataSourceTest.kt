package tj.umar.navoplayer.core.datastore.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class SettingsPreferencesDataSourceTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.dataSource() = SettingsPreferencesDataSource(
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tmp.root, "test.preferences_pb") },
        ),
    )

    @Test
    fun `defaults when nothing is stored`() = runTest {
        assertEquals(StoredSettings(0, emptySet(), true), dataSource().settings.first())
    }

    @Test
    fun `duration and headphones toggle are stored`() = runTest {
        val dataSource = dataSource()

        dataSource.setMinTrackDurationSeconds(30)
        dataSource.setPauseOnHeadphonesDisconnect(false)

        val settings = dataSource.settings.first()
        assertEquals(30, settings.minTrackDurationSeconds)
        assertEquals(false, settings.pauseOnHeadphonesDisconnect)
    }

    @Test
    fun `folders are added and removed`() = runTest {
        val dataSource = dataSource()

        dataSource.setFolderExcluded("Music/A", true)
        dataSource.setFolderExcluded("Music/B", true)
        dataSource.setFolderExcluded("Music/A", false)
        dataSource.setFolderExcluded("Music/Missing", false)

        assertEquals(setOf("Music/B"), dataSource.settings.first().excludedFolders)
    }

    private class FailingDataStore(private val error: Throwable) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw error }
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences = throw error
    }

    @Test
    fun `read io error gives defaults`() = runTest {
        val dataSource = SettingsPreferencesDataSource(FailingDataStore(IOException("disk")))

        assertEquals(StoredSettings(0, emptySet(), true), dataSource.settings.first())
    }

    @Test(expected = IllegalStateException::class)
    fun `other read errors are rethrown`() = runTest {
        SettingsPreferencesDataSource(FailingDataStore(IllegalStateException("bug"))).settings.first()
    }

    @Test
    fun `corrupted file is replaced with defaults`() = runTest {
        val file = File(tmp.root, "broken.preferences_pb").apply { writeBytes(byteArrayOf(1, 2, 3, 4, 5)) }
        val dataSource = SettingsPreferencesDataSource(
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                scope = backgroundScope,
                produceFile = { file },
            ),
        )

        assertEquals(StoredSettings(0, emptySet(), true), dataSource.settings.first())
    }
}
