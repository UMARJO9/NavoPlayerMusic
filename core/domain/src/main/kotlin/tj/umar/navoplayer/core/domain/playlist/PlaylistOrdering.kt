package tj.umar.navoplayer.core.domain.playlist

import tj.umar.navoplayer.core.domain.model.Track

fun List<Long>.movedTo(id: Long, toIndex: Int): List<Long> {
    val fromIndex = indexOf(id)
    if (fromIndex < 0) return this
    val target = toIndex.coerceIn(0, lastIndex)
    if (target == fromIndex) return this
    return toMutableList().apply { add(target, removeAt(fromIndex)) }
}

fun List<Track>.projected(order: List<Long>?, hidden: Set<Long>): List<Track> {
    val shown = if (hidden.isEmpty()) this else filterNot { it.id in hidden }
    if (order == null) return shown
    val rank = order.withIndex().associate { (index, id) -> id to index }
    val ordered = shown.filter { it.id in rank }.sortedBy { rank.getValue(it.id) }
    val rest = shown.filter { it.id !in rank }
    return ordered + rest
}

fun List<Track>.matchesOrder(order: List<Long>): Boolean {
    val ids = map { it.id }
    val present = ids.toSet()
    val ordered = order.toSet()
    return ids.filter { it in ordered } == order.filter { it in present }
}
