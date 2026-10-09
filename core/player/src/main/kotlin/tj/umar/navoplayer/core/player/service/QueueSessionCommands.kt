package tj.umar.navoplayer.core.player.service

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import tj.umar.navoplayer.core.domain.model.QueueInsertion

internal sealed interface QueueRequest {
    data class Enqueue(val items: List<MediaItem>, val insertion: QueueInsertion) : QueueRequest
    data class Remove(val queueItemId: String) : QueueRequest
    data class Move(val queueItemId: String, val toIndex: Int) : QueueRequest
}

internal data class QueueCommand(val command: SessionCommand, val args: Bundle)

@OptIn(UnstableApi::class)
internal object QueueSessionCommands {

    private const val ACTION_ENQUEUE = "tj.umar.navoplayer.queue.ENQUEUE"
    private const val ACTION_REMOVE = "tj.umar.navoplayer.queue.REMOVE"
    private const val ACTION_MOVE = "tj.umar.navoplayer.queue.MOVE"
    private const val KEY_ITEMS = "items"
    private const val KEY_INSERTION = "insertion"
    private const val KEY_QUEUE_ITEM_ID = "queue_item_id"
    private const val KEY_TO_INDEX = "to_index"

    val all: List<SessionCommand> = listOf(ACTION_ENQUEUE, ACTION_REMOVE, ACTION_MOVE)
        .map { SessionCommand(it, Bundle.EMPTY) }

    fun encode(request: QueueRequest): QueueCommand = when (request) {
        is QueueRequest.Enqueue -> command(ACTION_ENQUEUE) {
            putParcelableArrayList(KEY_ITEMS, ArrayList(request.items.map { it.toBundleIncludeLocalConfiguration() }))
            putString(KEY_INSERTION, request.insertion.name)
        }
        is QueueRequest.Remove -> command(ACTION_REMOVE) {
            putString(KEY_QUEUE_ITEM_ID, request.queueItemId)
        }
        is QueueRequest.Move -> command(ACTION_MOVE) {
            putString(KEY_QUEUE_ITEM_ID, request.queueItemId)
            putInt(KEY_TO_INDEX, request.toIndex)
        }
    }

    fun decode(command: SessionCommand, args: Bundle): QueueRequest? = runCatching {
        when (command.customAction) {
            ACTION_ENQUEUE -> {
                val insertion = QueueInsertion.entries.firstOrNull { it.name == args.getString(KEY_INSERTION) }
                @Suppress("DEPRECATION")
                val bundles = args.getParcelableArrayList<Bundle>(KEY_ITEMS)
                if (insertion == null || bundles.isNullOrEmpty()) null
                else QueueRequest.Enqueue(bundles.map(MediaItem::fromBundle), insertion)
            }
            ACTION_REMOVE -> args.getString(KEY_QUEUE_ITEM_ID)?.let(QueueRequest::Remove)
            ACTION_MOVE -> {
                val id = args.getString(KEY_QUEUE_ITEM_ID)
                if (id == null || !args.containsKey(KEY_TO_INDEX)) null
                else QueueRequest.Move(id, args.getInt(KEY_TO_INDEX))
            }
            else -> null
        }
    }.getOrNull()

    private fun command(action: String, fill: Bundle.() -> Unit): QueueCommand =
        QueueCommand(SessionCommand(action, Bundle.EMPTY), Bundle().apply(fill))
}
