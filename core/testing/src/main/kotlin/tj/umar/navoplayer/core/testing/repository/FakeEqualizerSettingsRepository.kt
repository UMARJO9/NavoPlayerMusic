package tj.umar.navoplayer.core.testing.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import tj.umar.navoplayer.core.domain.repository.EqualizerSettingsRepository

class FakeEqualizerSettingsRepository(
    initial: EqualizerSettings = EqualizerSettings(),
) : EqualizerSettingsRepository {

    private val settings = MutableStateFlow(initial)
    private val levelWrites = mutableListOf<List<Int>>()

    var observeError: Throwable? = null

    var writeError: Throwable? = null

    var writeGate: CompletableDeferred<Unit>? = null

    var writeCalls: Int = 0
        private set

    val customLevelWrites: List<List<Int>>
        get() = levelWrites.toList()

    val current: EqualizerSettings
        get() = settings.value

    override fun observeSettings(): Flow<EqualizerSettings> = flow {
        observeError?.let { throw it }
        emitAll(settings)
    }

    override suspend fun setEnabled(enabled: Boolean) {
        write { copy(enabled = enabled) }
    }

    override suspend fun selectPreset(selection: EqualizerPresetSelection) {
        write { copy(preset = selection) }
    }

    override suspend fun setCustomBandLevels(levelsMb: List<Int>) {
        write {
            levelWrites += levelsMb
            copy(preset = EqualizerPresetSelection.Custom, customBandLevelsMb = levelsMb)
        }
    }

    override suspend fun setBassBoostStrength(strength: Int) {
        write { copy(bassBoostStrength = strength) }
    }

    override suspend fun reset() {
        write { EqualizerSettings(enabled = enabled) }
    }

    fun emit(value: EqualizerSettings) {
        settings.value = value
    }

    private suspend fun write(transform: EqualizerSettings.() -> EqualizerSettings) {
        writeCalls++
        writeGate?.await()
        writeError?.let { throw it }
        settings.update(transform)
    }
}
