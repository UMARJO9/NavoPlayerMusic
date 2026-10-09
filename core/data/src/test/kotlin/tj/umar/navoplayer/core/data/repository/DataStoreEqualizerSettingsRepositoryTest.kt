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
import tj.umar.navoplayer.core.datastore.settings.EqualizerPreferencesDataSource
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import java.io.File

class DataStoreEqualizerSettingsRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.repository() = DataStoreEqualizerSettingsRepository(
        EqualizerPreferencesDataSource(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(tmp.root, "settings.preferences_pb") },
            ),
        ),
    )

    @Test
    fun `settings round trip`() = runTest {
        val repository = repository()

        repository.setEnabled(true)
        repository.selectPreset(EqualizerPresetSelection.Preset(1))
        repository.setBassBoostStrength(500)

        assertEquals(
            EqualizerSettings(enabled = true, preset = EqualizerPresetSelection.Preset(1), bassBoostStrength = 500),
            repository.observeSettings().first(),
        )
    }

    @Test
    fun `custom levels switch to custom`() = runTest {
        val repository = repository()
        repository.selectPreset(EqualizerPresetSelection.Preset(1))

        repository.setCustomBandLevels(listOf(100, -200))

        val settings = repository.observeSettings().first()
        assertEquals(EqualizerPresetSelection.Custom, settings.preset)
        assertEquals(listOf(100, -200), settings.customBandLevelsMb)
    }

    @Test
    fun `same value does not emit again`() = runTest {
        val repository = repository()

        repository.observeSettings().test {
            awaitItem()
            repository.setEnabled(false)
            expectNoEvents()
        }
    }

    @Test
    fun `reset keeps enabled`() = runTest {
        val repository = repository()
        repository.setEnabled(true)
        repository.setCustomBandLevels(listOf(300))

        repository.reset()

        assertEquals(EqualizerSettings(enabled = true), repository.observeSettings().first())
    }
}
