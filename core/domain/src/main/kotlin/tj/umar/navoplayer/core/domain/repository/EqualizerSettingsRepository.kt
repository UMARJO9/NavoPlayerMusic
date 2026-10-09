package tj.umar.navoplayer.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings

interface EqualizerSettingsRepository {
    fun observeSettings(): Flow<EqualizerSettings>
    suspend fun setEnabled(enabled: Boolean)
    suspend fun selectPreset(selection: EqualizerPresetSelection)
    suspend fun setCustomBandLevels(levelsMb: List<Int>)
    suspend fun setBassBoostStrength(strength: Int)
    suspend fun reset()
}
