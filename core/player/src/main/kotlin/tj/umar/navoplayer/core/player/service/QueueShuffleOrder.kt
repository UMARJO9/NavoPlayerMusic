package tj.umar.navoplayer.core.player.service

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder

@OptIn(UnstableApi::class)
internal class QueueShuffleOrder(playOrder: IntArray) : ShuffleOrder {

    private val order = playOrder.copyOf()
    private val positions = IntArray(order.size).also { positions ->
        order.forEachIndexed { position, index -> positions[index] = position }
    }

    val playOrder: IntArray
        get() = order.copyOf()

    override fun getLength(): Int = order.size

    override fun getNextIndex(index: Int): Int {
        val next = positions[index] + 1
        return if (next < order.size) order[next] else C.INDEX_UNSET
    }

    override fun getPreviousIndex(index: Int): Int {
        val previous = positions[index] - 1
        return if (previous >= 0) order[previous] else C.INDEX_UNSET
    }

    override fun getLastIndex(): Int = order.lastOrNull() ?: C.INDEX_UNSET

    override fun getFirstIndex(): Int = order.firstOrNull() ?: C.INDEX_UNSET

    override fun cloneAndInsert(insertionIndex: Int, insertionCount: Int): ShuffleOrder {
        val shifted = IntArray(order.size) { position ->
            val index = order[position]
            if (index >= insertionIndex) index + insertionCount else index
        }
        val inserted = IntArray(insertionCount) { insertionIndex + it }
        val anchorPosition = when {
            insertionIndex <= 0 -> -1
            insertionIndex >= order.size -> order.size - 1
            else -> positions[insertionIndex - 1]
        }
        return QueueShuffleOrder(shifted.insertedAfter(anchorPosition, inserted))
    }

    override fun cloneAndRemove(indexFrom: Int, indexToExclusive: Int): ShuffleOrder {
        val removedCount = indexToExclusive - indexFrom
        val remaining = order
            .filter { it < indexFrom || it >= indexToExclusive }
            .map { if (it >= indexToExclusive) it - removedCount else it }
        if (remaining.isEmpty()) return DefaultShuffleOrder(0)
        return QueueShuffleOrder(remaining.toIntArray())
    }

    override fun cloneAndMove(indexFrom: Int, indexToExclusive: Int, newIndexFrom: Int): ShuffleOrder {
        val windows = order.indices.toMutableList()
        val moved = windows.subList(indexFrom, indexToExclusive).toList()
        windows.subList(indexFrom, indexToExclusive).clear()
        windows.addAll(newIndexFrom, moved)
        val newIndexOf = IntArray(windows.size)
        windows.forEachIndexed { newIndex, oldIndex -> newIndexOf[oldIndex] = newIndex }
        return QueueShuffleOrder(IntArray(order.size) { newIndexOf[order[it]] })
    }

    override fun cloneAndClear(): ShuffleOrder = DefaultShuffleOrder(0)
}
