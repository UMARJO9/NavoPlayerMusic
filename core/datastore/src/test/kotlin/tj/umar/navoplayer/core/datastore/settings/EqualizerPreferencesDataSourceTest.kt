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

class EqualizerPreferencesDataSourceTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.dataSource() = EqualizerPreferencesDataSource(
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tmp.root, "test.preferences_pb") },
        ),
    )

    @Test
    fun `defaults when nothing is stored`() = runTest {
        assertEquals(StoredEqualizerSettings(), dataSource().settings.first())
    }

    @Test
    fun `values round trip`() = runTest {
        val dataSource = dataSource()

        dataSource.setEnabled(true)
        dataSource.setPresetIndex(2)
        dataSource.setBassBoostStrength(600)

        assertEquals(StoredEqualizerSettings(true, 2, null, 600), dataSource.settings.first())
    }

    @Test
    fun `custom levels clear preset`() = runTest {
        val dataSource = dataSource()
        dataSource.setPresetIndex(1)

        dataSource.setCustomLevels("0,300,-150")

        val stored = dataSource.settings.first()
        assertEquals(null, stored.presetIndex)
        assertEquals("0,300,-150", stored.customLevelsMb)
    }

    @Test
    fun `reset keeps enabled flag`() = runTest {
        val dataSource = dataSource()
        dataSource.setEnabled(true)
        dataSource.setPresetIndex(1)
        dataSource.setCustomLevels("100")
        dataSource.setBassBoostStrength(300)

        dataSource.reset()

        assertEquals(StoredEqualizerSettings(enabled = true), dataSource.settings.first())
    }
}
