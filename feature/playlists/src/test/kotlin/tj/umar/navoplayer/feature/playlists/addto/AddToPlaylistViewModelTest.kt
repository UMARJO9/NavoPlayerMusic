package tj.umar.navoplayer.feature.playlists.addto

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.usecase.AddTracksToPlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.CreatePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistsUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class AddToPlaylistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracks = FakeTrackRepository()
    private val playlists = FakePlaylistRepository(TestPlaylists.all)

    private val viewModel = AddToPlaylistViewModel(
        observePlaylists = ObservePlaylistsUseCase(playlists, ObserveTracksUseCase(tracks), mainDispatcherRule.testDispatcher),
        addTracksToPlaylist = AddTracksToPlaylistUseCase(playlists),
        createPlaylist = CreatePlaylistUseCase(playlists),
    )

    private var nextToken = 1L

    private suspend fun opened(vararg trackIds: Long): AddToPlaylistRequest {
        val request = AddToPlaylistRequest(nextToken++, trackIds.toList())
        viewModel.onIntent(AddToPlaylistIntent.Opened(request))
        tracks.emit(TestTracks.tracks)
        return request
    }

    @Test
    fun `opening loads playlists`() = runTest {
        opened(TestTracks.beta.id)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf(TestTracks.beta.id), state.trackIds)
        assertEquals(listOf(TestPlaylists.empty.id, TestPlaylists.morning.id), state.playlists.map { it.id })
    }

    @Test
    fun `playlist click adds track`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            assertEquals(AddToPlaylistEffect.Added(1, TestPlaylists.morning.name, 1), awaitItem())
        }
        assertTrue(TestTracks.beta.id in playlists.current.single { it.id == TestPlaylists.morning.id }.trackIds)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `adding present track reports zero`() = runTest {
        opened(TestTracks.alpha.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            assertEquals(AddToPlaylistEffect.Added(1, TestPlaylists.morning.name, 0), awaitItem())
        }
    }

    @Test
    fun `new playlist is created with track`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.onIntent(AddToPlaylistIntent.NewPlaylistClicked)
        assertTrue(viewModel.state.value.isNameFormVisible)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.NewPlaylistConfirmed("  Дорога "))
            assertEquals(AddToPlaylistEffect.Created(1, "Дорога"), awaitItem())
        }
        assertFalse(viewModel.state.value.isNameFormVisible)
        assertEquals(listOf(TestTracks.beta.id), playlists.current.single { it.name == "Дорога" }.trackIds)
    }

    @Test
    fun `name dialog can be dismissed`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.onIntent(AddToPlaylistIntent.NewPlaylistClicked)
        viewModel.onIntent(AddToPlaylistIntent.NameFormDismissed)

        assertFalse(viewModel.state.value.isNameFormVisible)
    }

    @Test
    fun `write failure reports failed`() = runTest {
        opened(TestTracks.beta.id)
        playlists.writeError = IllegalStateException("disk full")

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            assertEquals(AddToPlaylistEffect.Failed(1), awaitItem())
            viewModel.onIntent(AddToPlaylistIntent.NewPlaylistConfirmed("Дорога"))
            assertEquals(AddToPlaylistEffect.Failed(1), awaitItem())
        }
    }

    @Test
    fun `load failure can be retried`() = runTest {
        playlists.observeError = IllegalStateException("db closed")
        opened(TestTracks.beta.id)
        assertTrue(viewModel.state.value.loadFailed)

        playlists.observeError = null
        viewModel.onIntent(AddToPlaylistIntent.RetryLoad)

        assertFalse(viewModel.state.value.loadFailed)
        assertEquals(2, viewModel.state.value.playlists.size)
    }

    @Test
    fun `reopening resets state for new tracks`() = runTest {
        opened(TestTracks.beta.id)
        viewModel.onIntent(AddToPlaylistIntent.NewPlaylistClicked)

        opened(TestTracks.longMix.id)

        val state = viewModel.state.value
        assertEquals(listOf(TestTracks.longMix.id), state.trackIds)
        assertFalse(state.isNameFormVisible)
    }

    @Test
    fun `dismiss stops observing`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.onIntent(AddToPlaylistIntent.Dismissed)
        playlists.createPlaylist("Новый", emptyList())

        assertEquals(2, viewModel.state.value.playlists.size)
    }

    @Test
    fun `reopening same request keeps dialog and saving state`() = runTest {
        val request = opened(TestTracks.beta.id)
        viewModel.onIntent(AddToPlaylistIntent.NewPlaylistClicked)
        viewModel.onIntent(AddToPlaylistIntent.Dismissed)

        viewModel.onIntent(AddToPlaylistIntent.Opened(request))

        assertTrue(viewModel.state.value.isNameFormVisible)
        assertEquals(listOf(TestTracks.beta.id), viewModel.state.value.trackIds)
    }

    @Test
    fun `second tap while saving writes once`() = runTest {
        opened(TestTracks.beta.id)
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            viewModel.onIntent(AddToPlaylistIntent.NewPlaylistConfirmed("Дорога"))
            gate.complete(Unit)
            assertEquals(AddToPlaylistEffect.Added(1, TestPlaylists.morning.name, 1), awaitItem())
            expectNoEvents()
        }
        assertEquals(1, playlists.writeCalls)
    }

    @Test
    fun `result of previous opening is dropped`() = runTest {
        opened(TestTracks.beta.id)
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate
        viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))

        opened(TestTracks.longMix.id)

        viewModel.effects.test {
            gate.complete(Unit)
            expectNoEvents()
        }
        assertFalse(viewModel.state.value.isSaving)
    }
}
