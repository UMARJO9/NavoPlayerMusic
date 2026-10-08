package tj.umar.navoplayer.core.datastore.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

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
}
