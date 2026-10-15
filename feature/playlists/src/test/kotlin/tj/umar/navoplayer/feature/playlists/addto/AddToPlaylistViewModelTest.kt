package tj.umar.navoplayer.feature.playlists.addto

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.usecase.AddTracksToPlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.CreatePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveIsFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistsUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.SetFavoriteUseCase
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeFavoritesRepository
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class AddToPlaylistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracks = FakeTrackRepository()
    private val playlists = FakePlaylistRepository(TestPlaylists.all)
    private val favorites = FakeFavoritesRepository(listOf(TestTracks.alpha.id))

    private val viewModel = AddToPlaylistViewModel(
        observePlaylists = ObservePlaylistsUseCase(playlists, ObserveTracksUseCase(tracks, FakeSettingsRepository()), mainDispatcherRule.testDispatcher),
        addTracksToPlaylist = AddTracksToPlaylistUseCase(playlists),
        createPlaylist = CreatePlaylistUseCase(playlists),
        observeIsFavorite = ObserveIsFavoriteUseCase(favorites),
        setFavorite = SetFavoriteUseCase(favorites),
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

    @Test
    fun `playlist click marks playlist as saving`() = runTest {
        opened(TestTracks.beta.id)
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate

        viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))

        val state = viewModel.state.value
        assertTrue(state.isSaving)
        assertEquals(TestPlaylists.morning.id, state.savingPlaylistId)
        assertNull(state.addedPlaylistId)
        gate.complete(Unit)
    }

    @Test
    fun `successful add marks playlist as added`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            awaitItem()
        }

        val state = viewModel.state.value
        assertNull(state.savingPlaylistId)
        assertEquals(TestPlaylists.morning.id, state.addedPlaylistId)
    }

    @Test
    fun `failed add clears saving and added marks`() = runTest {
        opened(TestTracks.beta.id)
        playlists.writeError = IllegalStateException("disk full")

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            awaitItem()
        }

        val state = viewModel.state.value
        assertFalse(state.isSaving)
        assertNull(state.savingPlaylistId)
        assertNull(state.addedPlaylistId)
    }

    @Test
    fun `reopening clears added mark`() = runTest {
        val request = opened(TestTracks.beta.id)
        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            awaitItem()
        }
        viewModel.onIntent(AddToPlaylistIntent.Dismissed)

        viewModel.onIntent(AddToPlaylistIntent.Opened(request))
        assertNull(viewModel.state.value.addedPlaylistId)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            awaitItem()
        }
        opened(TestTracks.longMix.id)
        assertNull(viewModel.state.value.addedPlaylistId)
    }

    @Test
    fun `favorite toggle leaves playlist marks untouched`() = runTest {
        opened(TestTracks.beta.id)
        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
            assertEquals(AddToPlaylistEffect.FavoriteChanged(1, true), awaitItem())
        }

        val state = viewModel.state.value
        assertNull(state.savingPlaylistId)
        assertNull(state.addedPlaylistId)
    }

    @Test
    fun `actions are ignored after successful add`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            awaitItem()
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.empty.id))
            viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
            viewModel.onIntent(AddToPlaylistIntent.NewPlaylistClicked)
            expectNoEvents()
        }
        val state = viewModel.state.value
        assertTrue(state.isClosing)
        assertFalse(state.isNameFormVisible)
        assertEquals(1, playlists.writeCalls)
        assertEquals(0, favorites.writeCalls)
    }

    @Test
    fun `actions are ignored after favorite change`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
            awaitItem()
            viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            expectNoEvents()
        }
        assertTrue(viewModel.state.value.isClosing)
        assertEquals(1, favorites.writeCalls)
        assertEquals(0, playlists.writeCalls)
    }

    @Test
    fun `reopening unlocks sheet`() = runTest {
        val request = opened(TestTracks.beta.id)
        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))
            awaitItem()
        }

        viewModel.onIntent(AddToPlaylistIntent.Opened(request))

        assertFalse(viewModel.state.value.isClosing)
    }

    @Test
    fun `created playlist leaves added mark empty`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.NewPlaylistConfirmed("Дорога"))
            assertEquals(AddToPlaylistEffect.Created(1, "Дорога"), awaitItem())
        }
        assertNull(viewModel.state.value.addedPlaylistId)
        assertNull(viewModel.state.value.savingPlaylistId)
    }

    @Test
    fun `stale result keeps saving mark of new request`() = runTest {
        opened(TestTracks.beta.id)
        val firstGate = CompletableDeferred<Unit>()
        playlists.writeGate = firstGate
        viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.morning.id))

        opened(TestTracks.longMix.id)
        val secondGate = CompletableDeferred<Unit>()
        playlists.writeGate = secondGate
        viewModel.onIntent(AddToPlaylistIntent.PlaylistClicked(TestPlaylists.empty.id))
        firstGate.complete(Unit)

        assertEquals(TestPlaylists.empty.id, viewModel.state.value.savingPlaylistId)
        assertTrue(viewModel.state.value.isSaving)
        secondGate.complete(Unit)
    }

    @Test
    fun `single track request loads favorite state`() = runTest {
        opened(TestTracks.alpha.id)

        assertEquals(true, viewModel.state.value.isFavorite)
    }

    @Test
    fun `multi track request has no favorite state`() = runTest {
        opened(TestTracks.alpha.id, TestTracks.beta.id)

        assertNull(viewModel.state.value.isFavorite)
        assertNull(viewModel.state.value.favoriteTrackId)
    }

    @Test
    fun `favorite click toggles favorite`() = runTest {
        opened(TestTracks.beta.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
            assertEquals(AddToPlaylistEffect.FavoriteChanged(1, true), awaitItem())
        }
        assertTrue(TestTracks.beta.id in favorites.current)
        assertEquals(true, viewModel.state.value.isFavorite)
    }

    @Test
    fun `favorite click removes existing favorite`() = runTest {
        opened(TestTracks.alpha.id)

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
            assertEquals(AddToPlaylistEffect.FavoriteChanged(1, false), awaitItem())
        }
        assertFalse(TestTracks.alpha.id in favorites.current)
    }

    @Test
    fun `favorite failure is reported`() = runTest {
        opened(TestTracks.beta.id)
        favorites.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
            assertEquals(AddToPlaylistEffect.FavoriteFailed(1), awaitItem())
        }
    }

    @Test
    fun `new request resets favorite state`() = runTest {
        opened(TestTracks.alpha.id)

        opened(TestTracks.beta.id)

        assertEquals(false, viewModel.state.value.isFavorite)
    }

    @Test
    fun `favorite load error can be retried`() = runTest {
        favorites.observeError = IllegalStateException("db closed")
        opened(TestTracks.alpha.id)
        assertNull(viewModel.state.value.isFavorite)

        favorites.observeError = null
        viewModel.onIntent(AddToPlaylistIntent.RetryLoad)

        assertEquals(true, viewModel.state.value.isFavorite)
    }

    @Test
    fun `favorite click is ignored while saving or unknown`() = runTest {
        favorites.observeError = IllegalStateException("db closed")
        opened(TestTracks.beta.id)
        viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
        assertEquals(0, favorites.writeCalls)

        favorites.observeError = null
        viewModel.onIntent(AddToPlaylistIntent.RetryLoad)
        val gate = CompletableDeferred<Unit>()
        favorites.writeGate = gate
        viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
        viewModel.onIntent(AddToPlaylistIntent.FavoriteClicked)
        gate.complete(Unit)

        assertEquals(1, favorites.writeCalls)
    }
}
