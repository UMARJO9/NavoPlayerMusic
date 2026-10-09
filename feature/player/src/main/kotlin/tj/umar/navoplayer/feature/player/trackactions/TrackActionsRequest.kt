package tj.umar.navoplayer.feature.player.trackactions

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

data class TrackActionsRequest(
    val token: Long,
    val trackIds: List<Long>,
) {
    companion object {
        fun of(trackIds: List<Long>): TrackActionsRequest = TrackActionsRequest(System.nanoTime(), trackIds)

        val Saver: Saver<TrackActionsRequest?, Any> = listSaver<TrackActionsRequest?, Long>(
            save = { request -> request?.let { listOf(it.token) + it.trackIds }.orEmpty() },
            restore = { values -> values.firstOrNull()?.let { TrackActionsRequest(it, values.drop(1)) } },
        )
    }
}
