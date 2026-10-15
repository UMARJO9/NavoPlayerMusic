package tj.umar.navoplayer.artwork

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.domain.usecase.ObserveShowCoverArtUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CoverArtPreference @Inject constructor(
    observeShowCoverArt: ObserveShowCoverArtUseCase,
    @ApplicationScope scope: CoroutineScope,
) {
    val enabled: StateFlow<Boolean> = observeShowCoverArt()
        .catch { emit(true) }
        .stateIn(scope, SharingStarted.Eagerly, true)
}
