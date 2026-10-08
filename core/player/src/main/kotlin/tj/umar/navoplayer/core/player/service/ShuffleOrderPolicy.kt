package tj.umar.navoplayer.core.player.service

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import kotlin.random.Random

internal fun shuffledIndicesStartingAt(length: Int, startIndex: Int, random: Random): IntArray {
    if (length <= 0) return IntArray(0)
    val start = startIndex.coerceIn(0, length - 1)
    val rest = (0 until length).filter { it != start }.shuffled(random)
    return (listOf(start) + rest).toIntArray()
}

@OptIn(UnstableApi::class)
internal class ShuffleOrderListener(
    private val player: ExoPlayer,
    private val random: Random = Random.Default,
) : Player.Listener {

    private var appliedOrder: ShuffleOrder? = null

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        if (shuffleModeEnabled) applyShuffleOrder()
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason != Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED || !player.shuffleModeEnabled) return
        if (player.shuffleOrder !== appliedOrder) applyShuffleOrder()
    }

    private fun applyShuffleOrder() {
        val indices = shuffledIndicesStartingAt(player.mediaItemCount, player.currentMediaItemIndex, random)
        if (indices.isEmpty()) return
        val order = DefaultShuffleOrder(indices, random.nextLong())
        appliedOrder = order
        player.setShuffleOrder(order)
    }
}
