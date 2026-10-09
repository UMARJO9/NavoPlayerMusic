package tj.umar.navoplayer.core.datastore.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.datastore.di.SettingsDataStore
import java.io.IOException
import javax.inject.Inject

private val EqualizerEnabled = booleanPreferencesKey("equalizer_enabled")
private val EqualizerPresetIndex = intPreferencesKey("equalizer_preset_index")
private val EqualizerCustomLevels = stringPreferencesKey("equalizer_custom_levels_mb")
private val EqualizerBassBoostStrength = intPreferencesKey("equalizer_bass_boost_strength")

data class StoredEqualizerSettings(
    val enabled: Boolean = false,
    val presetIndex: Int? = null,
    val customLevelsMb: String? = null,
    val bassBoostStrength: Int = 0,
)

class EqualizerPreferencesDataSource @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
) {
    val settings: Flow<StoredEqualizerSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences ->
            StoredEqualizerSettings(
                enabled = preferences[EqualizerEnabled] ?: false,
                presetIndex = preferences[EqualizerPresetIndex],
                customLevelsMb = preferences[EqualizerCustomLevels],
                bassBoostStrength = preferences[EqualizerBassBoostStrength] ?: 0,
            )
        }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { it[EqualizerEnabled] = enabled }
    }

    suspend fun setPresetIndex(index: Int?) {
        dataStore.edit { preferences ->
            if (index == null) preferences.remove(EqualizerPresetIndex) else preferences[EqualizerPresetIndex] = index
        }
    }

    suspend fun setCustomLevels(levels: String) {
        dataStore.edit { preferences ->
            preferences[EqualizerCustomLevels] = levels
            preferences.remove(EqualizerPresetIndex)
        }
    }

    suspend fun setBassBoostStrength(strength: Int) {
        dataStore.edit { it[EqualizerBassBoostStrength] = strength }
    }

    suspend fun reset() {
        dataStore.edit { preferences ->
            preferences.remove(EqualizerPresetIndex)
            preferences.remove(EqualizerCustomLevels)
            preferences.remove(EqualizerBassBoostStrength)
        }
    }
}
