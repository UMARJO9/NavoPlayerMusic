package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.InvalidPlaylistNameException
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository

class PlaylistMutationUseCasesTest {

    private val repository = FakePlaylistRepository(TestPlaylists.all)

    private fun playlist(id: Long) = repository.current.single { it.id == id }

    @Test
    fun `create normalizes name and dedupes tracks`() = runTest {
        val result = CreatePlaylistUseCase(repository)("  Вечер   дома ", listOf(1, 2, 1))

        val created = (result as NavoResult.Success).data
        val id = created.id
        assertEquals("Вечер дома", created.name)
        assertEquals("Вечер дома", playlist(id).name)
        assertEquals(listOf(1L, 2L), playlist(id).trackIds)
    }

    @Test
    fun `create with blank name fails without writing`() = runTest {
        val result = CreatePlaylistUseCase(repository)("   ")

        assertTrue((result as NavoResult.Error).throwable is InvalidPlaylistNameException)
        assertEquals(2, repository.current.size)
    }

    @Test
    fun `create reports repository failure`() = runTest {
        repository.writeError = IllegalStateException("disk full")

        val result = CreatePlaylistUseCase(repository)("Mix")

        assertEquals("disk full", (result as NavoResult.Error).throwable.message)
    }

    @Test
    fun `rename stores normalized name`() = runTest {
        val result = RenamePlaylistUseCase(repository)(TestPlaylists.empty.id, " Новое ")

        assertEquals(NavoResult.Success(Unit), result)
        assertEquals("Новое", playlist(TestPlaylists.empty.id).name)
    }

    @Test
    fun `rename rejects invalid name and missing playlist`() = runTest {
        val rename = RenamePlaylistUseCase(repository)

        assertTrue((rename(TestPlaylists.empty.id, "") as NavoResult.Error).throwable is InvalidPlaylistNameException)
        assertTrue((rename(404, "Name") as NavoResult.Error).throwable is NoSuchElementException)
    }

    @Test
    fun `delete removes playlist`() = runTest {
        val result = DeletePlaylistUseCase(repository)(TestPlaylists.empty.id)

        assertEquals(NavoResult.Success(Unit), result)
        assertTrue(repository.current.none { it.id == TestPlaylists.empty.id })
    }

    @Test
    fun `add returns count of new tracks`() = runTest {
        val add = AddTracksToPlaylistUseCase(repository)

        assertEquals(NavoResult.Success(1), add(TestPlaylists.morning.id, listOf(TestTracks.beta.id, TestTracks.beta.id)))
        assertEquals(NavoResult.Success(0), add(TestPlaylists.morning.id, listOf(TestTracks.alpha.id)))
        assertEquals(NavoResult.Success(0), add(TestPlaylists.morning.id, emptyList()))
    }

    @Test
    fun `add reports failure`() = runTest {
        repository.writeError = IllegalStateException("locked")

        val result = AddTracksToPlaylistUseCase(repository)(TestPlaylists.morning.id, listOf(5))

        assertTrue(result is NavoResult.Error)
    }

    @Test
    fun `remove deletes track from playlist`() = runTest {
        val result = RemoveTrackFromPlaylistUseCase(repository)(TestPlaylists.morning.id, TestTracks.alpha.id)

        assertEquals(NavoResult.Success(Unit), result)
        assertTrue(TestTracks.alpha.id !in playlist(TestPlaylists.morning.id).trackIds)
    }
}
