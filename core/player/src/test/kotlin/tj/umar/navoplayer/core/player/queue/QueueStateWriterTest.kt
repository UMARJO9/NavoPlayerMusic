package tj.umar.navoplayer.core.player.queue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.domain.usecase.ClearSavedPlaybackQueueUseCase
import tj.umar.navoplayer.core.domain.usecase.SavePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.SavePlaybackQueueUseCase
import tj.umar.navoplayer.core.testing.data.TestSavedQueues
import tj.umar.navoplayer.core.testing.repository.FakePlaybackQueueRepository

@OptIn(ExperimentalCoroutinesApi::class)
class QueueStateWriterTest {

    private val repository = FakePlaybackQueueRepository()
    private val progress = SavedQueueProgress(0, 9_000, shuffleEnabled = false, repeatMode = RepeatMode.One)

    private fun TestScope.writer() = QueueStateWriter(
        SavePlaybackQueueUseCase(repository),
        SavePlaybackProgressUseCase(repository),
        ClearSavedPlaybackQueueUseCase(repository),
        backgroundScope,
    )

    @Test
    fun `full and progress merge into one save`() = runTest {
        val writer = writer()

        writer.submit(QueueWrite.Full(TestSavedQueues.alphaBeta))
        writer.submit(QueueWrite.Progress(progress))
        runCurrent()

        assertEquals(1, repository.saveCalls)
        assertEquals(0, repository.progressCalls)
        assertEquals(TestSavedQueues.alphaBeta.copy(progress = progress), repository.current)
    }

    @Test
    fun `writes apply in order`() = runTest {
        val writer = writer()
        writer.submit(QueueWrite.Full(TestSavedQueues.alphaBeta))
        runCurrent()

        writer.submit(QueueWrite.Progress(progress))
        runCurrent()
        writer.submit(QueueWrite.Clear)
        runCurrent()

        assertEquals(1, repository.progressCalls)
        assertNull(repository.current)
    }

    @Test
    fun `failure does not stop writer`() = runTest {
        val writer = writer()
        repository.writeError = IllegalStateException("disk")
        writer.submit(QueueWrite.Full(TestSavedQueues.alphaBeta))
        runCurrent()

        repository.writeError = null
        writer.submit(QueueWrite.Full(TestSavedQueues.alphaBeta))
        runCurrent()

        assertEquals(TestSavedQueues.alphaBeta, repository.current)
    }

    @Test
    fun `merge rules`() {
        val full = QueueWrite.Full(TestSavedQueues.alphaBeta)
        val update = QueueWrite.Progress(progress)

        assertEquals(QueueWrite.Full(TestSavedQueues.alphaBeta.copy(progress = progress)), full.mergedWith(update))
        assertEquals(QueueWrite.Clear, full.mergedWith(QueueWrite.Clear))
        assertEquals(QueueWrite.Clear, QueueWrite.Clear.mergedWith(update))
        assertEquals(full, QueueWrite.Clear.mergedWith(full))
        assertEquals(update, null.mergedWith(update))
    }
}
