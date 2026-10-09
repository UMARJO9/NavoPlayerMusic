package tj.umar.navoplayer.core.testing.data

import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.QueueItem
import tj.umar.navoplayer.core.domain.model.QueueItemId

object TestPlaybackQueues {

    val alphaItem = QueueItem(QueueItemId("q1"), TestTracks.alpha)
    val betaItem = QueueItem(QueueItemId("q2"), TestTracks.beta)
    val alphaAgainItem = QueueItem(QueueItemId("q3"), TestTracks.alpha)

    val alphaBetaAlpha = PlaybackQueue(listOf(alphaItem, betaItem, alphaAgainItem), currentIndex = 0)
}
