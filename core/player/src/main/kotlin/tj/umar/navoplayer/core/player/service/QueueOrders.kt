package tj.umar.navoplayer.core.player.service

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline

internal fun IntArray.withItemMoved(from: Int, to: Int): IntArray {
    if (from !in indices) return copyOf()
    val list = toMutableList()
    val moved = list.removeAt(from)
    list.add(to.coerceIn(0, list.size), moved)
    return list.toIntArray()
}

internal fun IntArray.insertedAfter(anchorPosition: Int, inserted: IntArray): IntArray {
    val split = (anchorPosition + 1).coerceIn(0, size)
    return copyOfRange(0, split) + inserted + copyOfRange(split, size)
}

internal fun Timeline.playOrder(shuffle: Boolean): IntArray {
    val count = windowCount
    if (count == 0) return IntArray(0)
    if (!shuffle) return IntArray(count) { it }
    val order = IntArray(count)
    var visited = 0
    var index = getFirstWindowIndex(true)
    while (index != C.INDEX_UNSET && visited < count) {
        order[visited++] = index
        index = getNextWindowIndex(index, Player.REPEAT_MODE_OFF, true)
    }
    return if (visited == count) order else order.copyOf(visited)
}
