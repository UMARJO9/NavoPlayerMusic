package tj.umar.navoplayer.core.player.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.domain.usecase.ObserveSettingsUseCase
import javax.inject.Inject

internal class AudioBecomingNoisyPolicy @Inject constructor(
    private val observeSettings: ObserveSettingsUseCase,
) {
    fun pauseOnDisconnect(): Flow<Boolean> =
        observeSettings()
            .map { it.pauseOnHeadphonesDisconnect }
            .catch { emit(true) }
            .distinctUntilChanged()
}
