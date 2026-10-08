package tj.umar.navoplayer.core.player.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

internal const val PROGRESS_TICK_MILLIS = 500L

@OptIn(ExperimentalCoroutinesApi::class)
internal fun progressTicks(triggers: Flow<Boolean>, intervalMs: Long = PROGRESS_TICK_MILLIS): Flow<Unit> =
    triggers.flatMapLatest { playing ->
        if (playing) {
            flow {
                while (true) {
                    emit(Unit)
                    delay(intervalMs)
                }
            }
        } else {
            flowOf(Unit)
        }
    }
