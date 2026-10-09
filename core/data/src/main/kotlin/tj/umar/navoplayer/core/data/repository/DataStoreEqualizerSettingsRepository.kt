package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.data.mapper.toBandLevelsStorage
import tj.umar.navoplayer.core.data.mapper.toEqualizerSettings
import tj.umar.navoplayer.core.datastore.settings.EqualizerPreferencesDataSource
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import tj.umar.navoplayer.core.domain.repository.EqualizerSettingsRepository
import javax.inject.Inject

internal class DataStoreEqualizerSettingsRepository @Inject constructor(
    private val dataSource: EqualizerPreferencesDataSource,
) : EqualizerSettingsRepository {

    override fun observeSettings(): Flow<EqualizerSettings> =
        dataSource.settings.map { it.toEqualizerSettings() }.distinctUntilChanged()

    override suspend fun setEnabled(enabled: Boolean) {
        dataSource.setEnabled(enabled)
    }

    override suspend fun selectPreset(selection: EqualizerPresetSelection) {
        dataSource.setPresetIndex((selection as? EqualizerPresetSelection.Preset)?.index)
    }

    override suspend fun setCustomBandLevels(levelsMb: List<Int>) {
        dataSource.setCustomLevels(levelsMb.toBandLevelsStorage())
    }

    override suspend fun setBassBoostStrength(strength: Int) {
        dataSource.setBassBoostStrength(strength)
    }

    override suspend fun reset() {
        dataSource.reset()
    }
}
