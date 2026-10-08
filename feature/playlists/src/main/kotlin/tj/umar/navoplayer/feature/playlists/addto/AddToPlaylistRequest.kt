package tj.umar.navoplayer.feature.playlists.addto

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

data class AddToPlaylistRequest(
    val token: Long,
    val trackIds: List<Long>,
) {
    companion object {
        fun of(trackIds: List<Long>): AddToPlaylistRequest = AddToPlaylistRequest(System.nanoTime(), trackIds)

        val Saver: Saver<AddToPlaylistRequest?, Any> = listSaver<AddToPlaylistRequest?, Long>(
            save = { request -> request?.let { listOf(it.token) + it.trackIds }.orEmpty() },
            restore = { values -> values.firstOrNull()?.let { AddToPlaylistRequest(it, values.drop(1)) } },
        )
    }
}
