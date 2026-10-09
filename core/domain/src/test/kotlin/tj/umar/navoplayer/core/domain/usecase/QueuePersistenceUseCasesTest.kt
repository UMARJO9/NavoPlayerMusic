package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.QueueResumeResult
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.testing.app.FakeAudioAccessChecker
import tj.umar.navoplayer.core.testing.data.TestSavedQueues
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakePlaybackQueueRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class QueuePersistenceUseCasesTest {

    private val queues = FakePlaybackQueueRepository(TestSavedQueues.alphaBeta)
    private val tracks = FakeTrackRepository()
    private val access = FakeAudioAccessChecker()
    private val load = LoadResumableQueueUseCase(queues, tracks, access)

    @Test
    fun `nothing saved`() = runTest {
        val empty = LoadResumableQueueUseCase(FakePlaybackQueueRepository(), tracks, access)

        assertEquals(QueueResumeResult.NothingSaved, empty())
    }

    @Test
    fun `saved queue resolves against device tracks`() = runTest {
        tracks.emit(listOf(TestTracks.alpha, TestTracks.beta))

        val result = load() as QueueResumeResult.Resumable

        assertEquals(listOf(TestTracks.alpha, TestTracks.beta), result.queue.items.map { it.track })
        assertEquals(1, result.queue.startIndex)
        assertEquals(12_000L, result.queue.startPositionMs)
    }

    @Test
    fun `missing permission keeps saved queue`() = runTest {
        access.granted = false

        assertEquals(QueueResumeResult.AccessDenied, load())
        assertEquals(0, queues.clearCalls)
    }

    @Test
    fun `all tracks missing clears saved queue`() = runTest {
        tracks.emit(emptyList())

        assertEquals(QueueResumeResult.AllTracksMissing, load())
        assertNull(queues.current)
    }

    @Test
    fun `failures do not clear saved queue`() = runTest {
        tracks.getTracksError = IllegalStateException("denied")

        assertEquals(QueueResumeResult.Failed, load())
        assertEquals(0, queues.clearCalls)
    }

    @Test
    fun `writes delegate to repository`() = runTest {
        val repository = FakePlaybackQueueRepository()
        SavePlaybackQueueUseCase(repository)(TestSavedQueues.alphaBeta)
        val progress = SavedQueueProgress(0, 5_000, shuffleEnabled = true, repeatMode = RepeatMode.One)
        SavePlaybackProgressUseCase(repository)(progress)

        assertEquals(progress, repository.current?.progress)

        ClearSavedPlaybackQueueUseCase(repository)()
        assertNull(repository.current)
    }
}
