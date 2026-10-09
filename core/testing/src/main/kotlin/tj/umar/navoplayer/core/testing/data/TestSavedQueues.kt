package tj.umar.navoplayer.core.testing.data

import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueItem
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress

object TestSavedQueues {

    val alphaBeta = SavedQueue(
        items = listOf(
            SavedQueueItem(QueueItemId("q1"), TestTracks.alpha.id),
            SavedQueueItem(QueueItemId("q2"), TestTracks.beta.id),
        ),
        shuffleOrder = null,
        progress = SavedQueueProgress(currentIndex = 1, positionMs = 12_000, shuffleEnabled = false, repeatMode = RepeatMode.Off),
        source = PlaybackSource.AllTracks,
    )
}
