package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.equalizer.resolve
import tj.umar.navoplayer.core.domain.equalizer.snapLevel
import tj.umar.navoplayer.core.domain.model.BASS_BOOST_MAX_STRENGTH
import tj.umar.navoplayer.core.domain.model.EqualizerAvailability
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerStatus
import tj.umar.navoplayer.core.domain.playback.EqualizerController
import tj.umar.navoplayer.core.domain.repository.EqualizerSettingsRepository
import javax.inject.Inject

class ObserveEqualizerUseCase @Inject constructor(
    private val controller: EqualizerController,
    private val repository: EqualizerSettingsRepository,
) {
    operator fun invoke(): Flow<EqualizerStatus> =
        combine(controller.observeAvailability(), repository.observeSettings()) { availability, settings ->
            when (availability) {
                EqualizerAvailability.Probing -> EqualizerStatus.Probing
                EqualizerAvailability.Unsupported -> EqualizerStatus.Unsupported(settings)
                is EqualizerAvailability.Supported -> EqualizerStatus.Ready(
                    capabilities = availability.capabilities,
                    settings = settings,
                    profile = settings.resolve(availability.capabilities),
                )
            }
        }.distinctUntilChanged()
}

class SetEqualizerEnabledUseCase @Inject constructor(
    private val repository: EqualizerSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): NavoResult<Unit> = navoRunCatching { repository.setEnabled(enabled) }
}

class SelectEqualizerPresetUseCase @Inject constructor(
    private val repository: EqualizerSettingsRepository,
) {
    suspend operator fun invoke(selection: EqualizerPresetSelection): NavoResult<Unit> =
        navoRunCatching { repository.selectPreset(selection) }
}

class SetEqualizerBandLevelUseCase @Inject constructor(
    private val observeEqualizer: ObserveEqualizerUseCase,
    private val repository: EqualizerSettingsRepository,
) {
    suspend operator fun invoke(band: Int, levelMb: Int): NavoResult<Unit> = navoRunCatching {
        val status = observeEqualizer().first()
        check(status is EqualizerStatus.Ready) { "Equalizer is not ready" }
        val levels = status.profile.bandLevelsMb
        require(band in levels.indices) { "Unknown band $band" }
        val updated = levels.toMutableList().apply { set(band, status.capabilities.snapLevel(levelMb)) }
        repository.setCustomBandLevels(updated)
    }
}

class SetBassBoostStrengthUseCase @Inject constructor(
    private val repository: EqualizerSettingsRepository,
) {
    suspend operator fun invoke(strength: Int): NavoResult<Unit> =
        navoRunCatching { repository.setBassBoostStrength(strength.coerceIn(0, BASS_BOOST_MAX_STRENGTH)) }
}

class ResetEqualizerUseCase @Inject constructor(
    private val repository: EqualizerSettingsRepository,
) {
    suspend operator fun invoke(): NavoResult<Unit> = navoRunCatching { repository.reset() }
}
