package tj.umar.navoplayer.feature.player.queue

import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.QueueItem
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.Track

private val previewTracks = listOf(
    Track(1, "Утро в Варзобе", "Navo Band", "Варзоб", 10, 100, 185_000, 1, "content://media/1", "Music/Navo", null, null),
    Track(2, "Ҷавонӣ", "Daler Nazarov", null, 11, 101, 252_000, 2, "content://media/2", "Music/Navo", null, null),
    Track(3, "Памир", null, null, null, null, 201_000, null, "content://media/3", "Download", null, null),
    Track(4, "Гиссар", "Navo Band", "Варзоб", 10, 100, 174_000, 3, "content://media/4", "Music/Navo", null, null),
    Track(5, "Душанбе", "Shabnam Surayo", null, 12, 102, 233_000, null, "content://media/5", "Music", null, null),
)

internal val previewQueueState = QueueState(
    isLoading = false,
    items = previewTracks.mapIndexed { index, track -> QueueItem(QueueItemId("q$index"), track) },
    currentItemId = QueueItemId("q1"),
    isPlaying = true,
    source = PlaybackSource.AllTracks,
)

internal val previewLongQueueState = previewQueueState.copy(
    items = (0 until 40).map { index ->
        val track = previewTracks[index % previewTracks.size]
        QueueItem(QueueItemId("long$index"), track.copy(id = index.toLong() + 1))
    },
    currentItemId = QueueItemId("long20"),
    shuffleEnabled = true,
)
