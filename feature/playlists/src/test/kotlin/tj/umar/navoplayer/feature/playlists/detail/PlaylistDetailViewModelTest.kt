package tj.umar.navoplayer.feature.playlists.detail

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RemovedPlaylistTrack
import tj.umar.navoplayer.core.domain.model.totalDurationMinutes
import tj.umar.navoplayer.core.domain.usecase.DeletePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.PlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveTrackFromPlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.RenamePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ReorderPlaylistTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.RestorePlaylistTrackUseCase
import tj.umar.navoplayer.core.domain.usecase.ShufflePlayTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class PlaylistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tracks = FakeTrackRepository()
    private val playlists = FakePlaylistRepository(TestPlaylists.all)
    private val playback = FakePlaybackController()

    private val morning = TestPlaylists.morning
    private val morningTracks = listOf(TestTracks.longMix, TestTracks.alpha)
    private val morningSource = PlaybackSource.Playlist(morning.id, morning.name)

    private fun viewModel(playlistId: Long = morning.id) = PlaylistDetailViewModel(
        playlistId = playlistId,
        observePlaylist = ObservePlaylistUseCase(playlists, ObserveTracksUseCase(tracks, FakeSettingsRepository()), mainDispatcherRule.testDispatcher),
        observePlaybackState = ObservePlaybackStateUseCase(playback),
        playTracks = PlayTracksUseCase(playback),
        shufflePlayTracks = ShufflePlayTracksUseCase(playback),
        togglePlayPause = TogglePlayPauseUseCase(playback),
        renamePlaylist = RenamePlaylistUseCase(playlists),
        deletePlaylist = DeletePlaylistUseCase(playlists),
        removeTrackFromPlaylist = RemoveTrackFromPlaylistUseCase(playlists),
        reorderPlaylistTracks = ReorderPlaylistTracksUseCase(playlists),
        restorePlaylistTrack = RestorePlaylistTrackUseCase(playlists),
    )

    private suspend fun PlaylistDetailViewModel.started(): PlaylistDetailViewModel = apply {
        onIntent(PlaylistDetailIntent.ScreenStarted(hasPermission = true))
        tracks.emit(TestTracks.tracks)
    }

    @Test
    fun `initial state is loading without subscription`() {
        val viewModel = viewModel()

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(0, playlists.observeCalls)
    }

    @Test
    fun `start without permission navigates to welcome`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.ScreenStarted(hasPermission = false))
            assertEquals(PlaylistDetailEffect.NavigateToWelcome, awaitItem())
        }
        assertEquals(0, playlists.observeCalls)
    }

    @Test
    fun `start loads available tracks and minutes`() = runTest {
        val state = viewModel().started().state.value

        assertFalse(state.isLoading)
        assertEquals(morningTracks, state.tracks)
        assertEquals(1, state.playlist?.missingTrackCount)
        assertEquals(morningTracks.totalDurationMinutes(), state.totalMinutes)
    }

    @Test
    fun `unknown playlist is reported missing`() = runTest {
        val state = viewModel(playlistId = 404).started().state.value

        assertTrue(state.isMissing)
    }

    @Test
    fun `load error can be retried`() = runTest {
        tracks.error = IllegalStateException("scan failed")
        val viewModel = viewModel().started()
        assertTrue(viewModel.state.value.loadFailed)

        tracks.error = null
        viewModel.onIntent(PlaylistDetailIntent.RetryLoad)
        tracks.emit(TestTracks.tracks)

        assertFalse(viewModel.state.value.loadFailed)
        assertEquals(morningTracks, viewModel.state.value.tracks)
    }

    @Test
    fun `play starts playlist from first track`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.PlayClicked)

        assertEquals(listOf(PlaybackCommand.Play(morningTracks, 0, morningSource)), playback.commands)
    }

    @Test
    fun `play toggles when playlist is already active`() = runTest {
        val viewModel = viewModel().started()
        playback.state.emit(TestPlaybackStates.playingAlpha.copy(source = PlaybackSource.Playlist(morning.id, "Old name")))

        assertTrue(viewModel.state.value.isPlaylistPlaying)
        viewModel.onIntent(PlaylistDetailIntent.PlayClicked)

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), playback.commands)
    }

    @Test
    fun `shuffle plays shuffled and ignores empty playlist`() = runTest {
        viewModel().started().onIntent(PlaylistDetailIntent.ShuffleClicked)
        viewModel(TestPlaylists.empty.id).started().onIntent(PlaylistDetailIntent.ShuffleClicked)

        assertEquals(listOf(PlaybackCommand.PlayShuffled(morningTracks, morningSource)), playback.commands)
    }

    @Test
    fun `track click plays at index and resumes current paused track`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.TrackClicked(TestTracks.alpha.id))
        playback.state.emit(TestPlaybackStates.pausedAlpha.copy(source = morningSource))
        viewModel.onIntent(PlaylistDetailIntent.TrackClicked(TestTracks.alpha.id))

        assertEquals(
            listOf(PlaybackCommand.Play(morningTracks, 1, morningSource), PlaybackCommand.TogglePlayPause),
            playback.commands,
        )
    }

    @Test
    fun `remove request hides track and offers undo`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
            assertEquals(PlaylistDetailEffect.ShowRemoveUndo(1, TestTracks.alpha.title), awaitItem())
        }
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
        assertTrue(viewModel.state.value.removingTrackIds.isEmpty())
        assertTrue(TestTracks.alpha.id !in storedOrder())
        assertEquals(listOf(TestTracks.longMix).totalDurationMinutes(), viewModel.state.value.totalMinutes)
    }

    @Test
    fun `long press removes with undo`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.TrackLongPressed(TestTracks.longMix.id))
            assertEquals(PlaylistDetailEffect.ShowRemoveUndo(1, TestTracks.longMix.title), awaitItem())
        }
        assertNull(viewModel.state.value.dialog)
        assertEquals(listOf(TestTracks.alpha), viewModel.state.value.tracks)
    }

    @Test
    fun `undo restores track at original index`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.longMix.id))
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))

        assertEquals(morning.trackIds, storedOrder())
        assertEquals(morningTracks, viewModel.state.value.tracks)
        assertEquals(listOf(RemovedPlaylistTrack(morning.id, TestTracks.longMix.id, 0)), playlists.restoreCalls)
    }

    @Test
    fun `undo after dismiss is ignored`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveDismissed(1))
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))

        assertTrue(playlists.restoreCalls.isEmpty())
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `new removal finalizes previous and stale token is ignored`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
            assertEquals(PlaylistDetailEffect.ShowRemoveUndo(1, TestTracks.alpha.title), awaitItem())
            viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.longMix.id))
            assertEquals(PlaylistDetailEffect.ShowRemoveUndo(2, TestTracks.longMix.title), awaitItem())
        }
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveDismissed(1))
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))
        assertTrue(playlists.restoreCalls.isEmpty())

        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(2))
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
        assertEquals(listOf(TestTracks.longMix.id, TestPlaylists.MISSING_TRACK_ID), storedOrder())
    }

    @Test
    fun `remove failure brings track back and shows message`() = runTest {
        val viewModel = viewModel().started()
        playlists.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
            assertEquals(PlaylistDetailEffect.ShowRemoveUndo(1, TestTracks.alpha.title), awaitItem())
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RemoveFailed), awaitItem())
        }
        assertEquals(morningTracks, viewModel.state.value.tracks)
        assertTrue(viewModel.state.value.removingTrackIds.isEmpty())
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))
        assertTrue(playlists.restoreCalls.isEmpty())
    }

    @Test
    fun `remove waits for write and hides track meanwhile`() = runTest {
        val viewModel = viewModel().started()
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate

        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
        assertEquals(setOf(TestTracks.alpha.id), viewModel.state.value.removingTrackIds)
        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))

        gate.complete(Unit)
        assertEquals(1, playlists.writeCalls)
        assertTrue(viewModel.state.value.removingTrackIds.isEmpty())
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `restore failure shows message`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
            awaitItem()
            playlists.writeError = IllegalStateException("locked")
            viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RestoreFailed), awaitItem())
        }
        assertEquals(listOf(TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `remove while deleting is ignored`() = runTest {
        val viewModel = viewModel().started()
        playlists.writeGate = CompletableDeferred()

        viewModel.onIntent(PlaylistDetailIntent.DeleteConfirmed)
        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))

        assertEquals(morningTracks, viewModel.state.value.tracks)
        assertEquals(1, playlists.writeCalls)
        playlists.writeGate?.complete(Unit)
    }

    fun `rename updates name`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.RenameClicked)
        assertEquals(PlaylistDetailDialog.Rename(morning.name), viewModel.state.value.dialog)
        viewModel.onIntent(PlaylistDetailIntent.RenameConfirmed("  Вечер "))

        assertNull(viewModel.state.value.dialog)
        assertEquals("Вечер", viewModel.state.value.playlist?.name)
    }

    @Test
    fun `rename failure shows message`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.RenameConfirmed("   "))
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.RenameFailed), awaitItem())
        }
    }

    @Test
    fun `delete navigates back without missing flash`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.DeleteClicked)
            assertEquals(PlaylistDetailDialog.ConfirmDelete, viewModel.state.value.dialog)
            viewModel.onIntent(PlaylistDetailIntent.DeleteConfirmed)
            assertEquals(PlaylistDetailEffect.NavigateBack, awaitItem())
        }
        assertFalse(viewModel.state.value.isMissing)
        assertTrue(playlists.current.none { it.id == morning.id })
    }

    @Test
    fun `delete failure keeps playlist and shows message`() = runTest {
        val viewModel = viewModel().started()
        playlists.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.DeleteConfirmed)
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.DeleteFailed), awaitItem())
        }
        assertFalse(viewModel.state.value.isDeleting)
    }

    @Test
    fun `dialog dismiss closes dialog`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.DeleteClicked)
        viewModel.onIntent(PlaylistDetailIntent.DialogDismissed)

        assertNull(viewModel.state.value.dialog)
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.BackClicked)
            assertEquals(PlaylistDetailEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `stop and restart resubscribe`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.ScreenStopped)
        viewModel.onIntent(PlaylistDetailIntent.ScreenStarted(hasPermission = true))

        assertEquals(2, playlists.observeCalls)
        assertEquals(morningTracks, viewModel.state.value.tracks)
    }

    private fun storedOrder() = playlists.current.single { it.id == morning.id }.trackIds

    @Test
    fun `move reorders tracks at once and commits after debounce`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))

        assertEquals(listOf(TestTracks.alpha, TestTracks.longMix), viewModel.state.value.tracks)
        advanceTimeBy(399)
        runCurrent()
        assertTrue(playlists.reorderCalls.isEmpty())
        advanceTimeBy(2)
        runCurrent()
        assertEquals(listOf(morning.id to listOf(TestTracks.alpha.id, TestTracks.longMix.id)), playlists.reorderCalls)
        assertEquals(listOf(TestTracks.alpha.id, TestPlaylists.MISSING_TRACK_ID, TestTracks.longMix.id), storedOrder())
        assertNull(viewModel.state.value.orderOverride)
        assertEquals(listOf(TestTracks.alpha, TestTracks.longMix), viewModel.state.value.tracks)
    }

    @Test
    fun `rapid moves commit only final order`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        advanceTimeBy(200)
        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 1))
        advanceTimeBy(200)
        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        advanceTimeBy(401)
        runCurrent()

        assertEquals(listOf(morning.id to listOf(TestTracks.alpha.id, TestTracks.longMix.id)), playlists.reorderCalls)
    }

    @Test
    fun `move to same position does nothing`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.longMix.id, 0))
        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(404, 1))
        advanceTimeBy(1_000)
        runCurrent()

        assertTrue(playlists.reorderCalls.isEmpty())
        assertNull(viewModel.state.value.orderOverride)
    }

    @Test
    fun `reorder does not bump playlist update time`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        advanceTimeBy(401)
        runCurrent()

        assertEquals(morning.updatedAtMs, playlists.current.single { it.id == morning.id }.updatedAtMs)
    }

    @Test
    fun `reorder failure reverts order and shows message`() = runTest {
        val viewModel = viewModel().started()
        playlists.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
            advanceTimeBy(401)
            runCurrent()
            assertEquals(PlaylistDetailEffect.ShowMessage(PlaylistDetailMessage.ReorderFailed), awaitItem())
        }
        assertEquals(morningTracks, viewModel.state.value.tracks)
        assertNull(viewModel.state.value.orderOverride)
    }

    @Test
    fun `screen stop flushes pending reorder`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        viewModel.onIntent(PlaylistDetailIntent.ScreenStopped)
        runCurrent()

        assertEquals(1, playlists.reorderCalls.size)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(1, playlists.reorderCalls.size)
    }

    @Test
    fun `back flushes pending reorder and navigates back`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
            viewModel.onIntent(PlaylistDetailIntent.BackClicked)
            assertEquals(PlaylistDetailEffect.NavigateBack, awaitItem())
        }
        runCurrent()
        assertEquals(listOf(TestTracks.alpha.id, TestPlaylists.MISSING_TRACK_ID, TestTracks.longMix.id), storedOrder())
    }

    @Test
    fun `stale emission keeps pending order`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        playlists.renamePlaylist(morning.id, "Вечер")

        assertEquals("Вечер", viewModel.state.value.playlist?.name)
        assertEquals(listOf(TestTracks.alpha, TestTracks.longMix), viewModel.state.value.tracks)
        assertEquals(listOf(TestTracks.alpha.id, TestTracks.longMix.id), viewModel.state.value.orderOverride)
    }

    @Test
    fun `track added during pending order goes to the end`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        playlists.addTracks(morning.id, listOf(TestTracks.beta.id))

        assertEquals(listOf(TestTracks.alpha, TestTracks.longMix, TestTracks.beta), viewModel.state.value.tracks)
    }

    @Test
    fun `play and track click use reordered tracks`() = runTest {
        val viewModel = viewModel().started()
        val reordered = listOf(TestTracks.alpha, TestTracks.longMix)

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        viewModel.onIntent(PlaylistDetailIntent.PlayClicked)
        viewModel.onIntent(PlaylistDetailIntent.TrackClicked(TestTracks.longMix.id))

        assertEquals(
            listOf(PlaybackCommand.Play(reordered, 0, morningSource), PlaybackCommand.Play(reordered, 1, morningSource)),
            playback.commands,
        )
    }

    @Test
    fun `move is ignored while deleting`() = runTest {
        val viewModel = viewModel().started()
        playlists.writeGate = CompletableDeferred()

        viewModel.onIntent(PlaylistDetailIntent.DeleteConfirmed)
        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))

        assertEquals(morningTracks, viewModel.state.value.tracks)
        playlists.writeGate?.complete(Unit)
    }

    private fun PlaylistDetailViewModel.cleared() {
        ViewModelStore().apply {
            put("detail", this@cleared)
            clear()
        }
    }

    @Test
    fun `removal write finishes after view model is cleared`() = runTest {
        val viewModel = viewModel().started()
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate

        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
        viewModel.cleared()
        gate.complete(Unit)
        runCurrent()

        assertTrue(TestTracks.alpha.id !in storedOrder())
    }

    @Test
    fun `restore write finishes after view model is cleared`() = runTest {
        val viewModel = viewModel().started()
        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate

        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))
        viewModel.cleared()
        gate.complete(Unit)
        runCurrent()

        assertEquals(morning.trackIds, storedOrder())
    }

    @Test
    fun `undo of track re-added elsewhere is treated as success`() = runTest {
        val viewModel = viewModel().started()

        viewModel.effects.test {
            viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
            awaitItem()
            playlists.addTracks(morning.id, listOf(TestTracks.alpha.id))
            viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))
            expectNoEvents()
        }
        assertEquals(1, playlists.restoreCalls.size)
        assertEquals(morningTracks, viewModel.state.value.tracks)
    }

    @Test
    fun `undo while removal write is in flight restores after it lands`() = runTest {
        val viewModel = viewModel().started()
        val gate = CompletableDeferred<Unit>()
        playlists.writeGate = gate

        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.alpha.id))
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))
        assertTrue(playlists.restoreCalls.isEmpty())
        gate.complete(Unit)

        assertEquals(listOf(RemovedPlaylistTrack(morning.id, TestTracks.alpha.id, 2)), playlists.restoreCalls)
        assertEquals(morning.trackIds, storedOrder())
        assertEquals(morningTracks, viewModel.state.value.tracks)
        assertTrue(viewModel.state.value.removingTrackIds.isEmpty())
    }

    @Test
    fun `move then remove then undo within debounce keeps moved order`() = runTest {
        val viewModel = viewModel().started()

        viewModel.onIntent(PlaylistDetailIntent.MoveTrack(TestTracks.alpha.id, 0))
        viewModel.onIntent(PlaylistDetailIntent.RemoveTrackRequested(TestTracks.longMix.id))
        assertEquals(listOf(TestTracks.alpha), viewModel.state.value.tracks)
        viewModel.onIntent(PlaylistDetailIntent.UndoRemoveClicked(1))
        assertEquals(listOf(TestTracks.alpha, TestTracks.longMix), viewModel.state.value.tracks)

        advanceTimeBy(401)
        runCurrent()

        assertEquals(listOf(TestTracks.alpha.id, TestPlaylists.MISSING_TRACK_ID, TestTracks.longMix.id), storedOrder())
        assertEquals(listOf(TestTracks.alpha, TestTracks.longMix), viewModel.state.value.tracks)
        assertNull(viewModel.state.value.orderOverride)
    }
}
