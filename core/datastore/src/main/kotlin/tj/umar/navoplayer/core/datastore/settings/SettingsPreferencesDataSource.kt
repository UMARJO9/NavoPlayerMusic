package tj.umar.navoplayer.core.datastore.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val MinTrackDurationSeconds = intPreferencesKey("min_track_duration_seconds")
private val ExcludedFolders = stringSetPreferencesKey("excluded_folders")
private val PauseOnHeadphonesDisconnect = booleanPreferencesKey("pause_on_headphones_disconnect")

class SettingsPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val settings: Flow<StoredSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences ->
            StoredSettings(
                minTrackDurationSeconds = preferences[MinTrackDurationSeconds] ?: 0,
                excludedFolders = preferences[ExcludedFolders].orEmpty(),
                pauseOnHeadphonesDisconnect = preferences[PauseOnHeadphonesDisconnect] ?: true,
            )
        }

    suspend fun setMinTrackDurationSeconds(seconds: Int) {
        dataStore.edit { it[MinTrackDurationSeconds] = seconds }
    }

    suspend fun setFolderExcluded(path: String, excluded: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[ExcludedFolders].orEmpty()
            preferences[ExcludedFolders] = if (excluded) current + path else current - path
        }
    }

    suspend fun setPauseOnHeadphonesDisconnect(enabled: Boolean) {
        dataStore.edit { it[PauseOnHeadphonesDisconnect] = enabled }
    }
}
