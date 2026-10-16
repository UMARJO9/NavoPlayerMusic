package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.RemovedPlaylistTrack
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository

class PlaylistOrderUseCasesTest {

    private val repository = FakePlaylistRepository(TestPlaylists.all)
    private val morningId = TestPlaylists.morning.id

    private fun trackIds() = repository.current.single { it.id == morningId }.trackIds

    @Test
    fun `restore returns track to its index`() = runTest {
        val removed = RemoveTrackFromPlaylistUseCase(repository)(morningId, TestPlaylists.MISSING_TRACK_ID)

        val result = RestorePlaylistTrackUseCase(repository)((removed as NavoResult.Success).data!!)

        assertEquals(NavoResult.Success(true), result)
        assertEquals(TestPlaylists.morning.trackIds, trackIds())
    }

    @Test
    fun `restore of present track reports false`() = runTest {
        val result = RestorePlaylistTrackUseCase(repository)(RemovedPlaylistTrack(morningId, TestTracks.alpha.id, 0))

        assertEquals(NavoResult.Success(false), result)
    }

    @Test
    fun `restore reports failure`() = runTest {
        repository.writeError = IllegalStateException("locked")

        val result = RestorePlaylistTrackUseCase(repository)(RemovedPlaylistTrack(morningId, TestTracks.beta.id, 0))

        assertTrue(result is NavoResult.Error)
    }

    @Test
    fun `reorder keeps hidden track slot`() = runTest {
        val result = ReorderPlaylistTracksUseCase(repository)(morningId, listOf(TestTracks.alpha.id, TestTracks.longMix.id))

        assertEquals(NavoResult.Success(true), result)
        assertEquals(listOf(TestTracks.alpha.id, TestPlaylists.MISSING_TRACK_ID, TestTracks.longMix.id), trackIds())
        assertEquals(listOf(morningId to listOf(TestTracks.alpha.id, TestTracks.longMix.id)), repository.reorderCalls)
    }

    @Test
    fun `reorder does not bump update time`() = runTest {
        ReorderPlaylistTracksUseCase(repository)(morningId, listOf(TestTracks.alpha.id, TestTracks.longMix.id))

        assertEquals(TestPlaylists.morning.updatedAtMs, repository.current.single { it.id == morningId }.updatedAtMs)
    }

    @Test
    fun `reorder reports failure`() = runTest {
        repository.writeError = IllegalStateException("locked")

        val result = ReorderPlaylistTracksUseCase(repository)(morningId, listOf(TestTracks.alpha.id))

        assertTrue(result is NavoResult.Error)
    }
}
