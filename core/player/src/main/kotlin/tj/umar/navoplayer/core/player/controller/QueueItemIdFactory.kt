package tj.umar.navoplayer.core.player.controller

import java.util.UUID
import javax.inject.Inject

internal fun interface QueueItemIdFactory {
    fun create(): String
}

internal class UuidQueueItemIdFactory @Inject constructor() : QueueItemIdFactory {
    override fun create(): String = UUID.randomUUID().toString()
}
