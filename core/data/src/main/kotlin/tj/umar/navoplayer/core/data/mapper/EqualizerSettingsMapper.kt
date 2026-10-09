package tj.umar.navoplayer.core.data.mapper

import tj.umar.navoplayer.core.datastore.settings.StoredEqualizerSettings
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings

private const val LEVEL_SEPARATOR = ","

internal fun StoredEqualizerSettings.toEqualizerSettings(): EqualizerSettings = EqualizerSettings(
    enabled = enabled,
    preset = presetIndex?.takeIf { it >= 0 }?.let(EqualizerPresetSelection::Preset) ?: EqualizerPresetSelection.Custom,
    customBandLevelsMb = parseBandLevels(customLevelsMb),
    bassBoostStrength = bassBoostStrength,
)

internal fun parseBandLevels(value: String?): List<Int> {
    if (value.isNullOrBlank()) return emptyList()
    val levels = value.split(LEVEL_SEPARATOR).map { it.trim().toIntOrNull() }
    return if (levels.any { it == null }) emptyList() else levels.filterNotNull()
}

internal fun List<Int>.toBandLevelsStorage(): String = joinToString(LEVEL_SEPARATOR)
