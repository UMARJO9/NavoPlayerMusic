package tj.umar.navoplayer.artwork

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.domain.usecase.ObserveShowCoverArtUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CoverArtPreference @Inject constructor(
    observeShowCoverArt: ObserveShowCoverArtUseCase,
    @ApplicationScope scope: CoroutineScope,
) {
    private val enabled: SharedFlow<Boolean> = observeShowCoverArt()
        .catch { emit(true) }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    val isKnownEnabled: Boolean
        get() = enabled.replayCache.lastOrNull() == true

    suspend fun isEnabled(): Boolean = enabled.first()
}
