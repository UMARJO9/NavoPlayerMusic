package tj.umar.navoplayer.feature.playlists.addto

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.usecase.AddTracksToPlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.CreatePlaylistUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveIsFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaylistsUseCase
import tj.umar.navoplayer.core.domain.usecase.SetFavoriteUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class AddToPlaylistViewModel @Inject constructor(
    private val observePlaylists: ObservePlaylistsUseCase,
    private val addTracksToPlaylist: AddTracksToPlaylistUseCase,
    private val createPlaylist: CreatePlaylistUseCase,
    private val observeIsFavorite: ObserveIsFavoriteUseCase,
    private val setFavorite: SetFavoriteUseCase,
) : MviViewModel<AddToPlaylistState, AddToPlaylistIntent, AddToPlaylistEffect>(AddToPlaylistState()) {

    private var playlistsJob: Job? = null
    private var favoriteJob: Job? = null

    override fun onIntent(intent: AddToPlaylistIntent) {
        when (intent) {
            is AddToPlaylistIntent.Opened -> onOpened(intent.request)
            is AddToPlaylistIntent.PlaylistClicked -> onPlaylistClicked(intent.playlistId)
            AddToPlaylistIntent.FavoriteClicked -> onFavoriteClicked()
            AddToPlaylistIntent.NewPlaylistClicked -> setState { copy(isNameFormVisible = true) }
            AddToPlaylistIntent.NameFormDismissed -> setState { copy(isNameFormVisible = false) }
            is AddToPlaylistIntent.NewPlaylistConfirmed -> onNewPlaylistConfirmed(intent.name)
            AddToPlaylistIntent.RetryLoad -> startObserving()
            AddToPlaylistIntent.Dismissed -> stopObserving()
        }
    }

    private fun onOpened(request: AddToPlaylistRequest) {
        if (request.token != currentState.token) {
            stopObserving()
            setState { AddToPlaylistState(token = request.token, trackIds = request.trackIds) }
        }
        startObserving()
    }

    private fun onPlaylistClicked(playlistId: Long) {
        val state = currentState
        val token = state.token ?: return
        if (state.isSaving || state.trackIds.isEmpty()) return
        val playlist = state.playlists.firstOrNull { it.id == playlistId } ?: return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val effect = when (val result = addTracksToPlaylist(playlistId, state.trackIds)) {
                is NavoResult.Success -> AddToPlaylistEffect.Added(token, playlist.name, result.data)
                is NavoResult.Error -> AddToPlaylistEffect.Failed(token)
            }
            finishSaving(effect)
        }
    }

    private fun onNewPlaylistConfirmed(name: String) {
        val state = currentState
        val token = state.token ?: return
        if (state.isSaving) return
        setState { copy(isSaving = true, isNameFormVisible = false) }
        viewModelScope.launch {
            val effect = when (val result = createPlaylist(name, state.trackIds)) {
                is NavoResult.Success -> AddToPlaylistEffect.Created(token, result.data.name)
                is NavoResult.Error -> AddToPlaylistEffect.Failed(token)
            }
            finishSaving(effect)
        }
    }

    private fun onFavoriteClicked() {
        val state = currentState
        val token = state.token ?: return
        val trackId = state.favoriteTrackId ?: return
        val isFavorite = state.isFavorite ?: return
        if (state.isSaving) return
        val target = !isFavorite
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val effect = when (setFavorite(trackId, target)) {
                is NavoResult.Success -> AddToPlaylistEffect.FavoriteChanged(token, target)
                is NavoResult.Error -> AddToPlaylistEffect.FavoriteFailed(token)
            }
            finishSaving(effect)
        }
    }

    private fun startObservingFavorite() {
        if (favoriteJob?.isActive == true) return
        val trackId = currentState.favoriteTrackId ?: return
        favoriteJob = observeIsFavorite(trackId)
            .onEach { isFavorite -> setState { copy(isFavorite = isFavorite) } }
            .catch { setState { copy(isFavorite = null) } }
            .launchIn(viewModelScope)
    }

    private fun finishSaving(effect: AddToPlaylistEffect) {
        if (effect.token != currentState.token) return
        setState { copy(isSaving = false) }
        sendEffect(effect)
    }

    private fun startObserving() {
        if (playlistsJob?.isActive == true) return
        setState { copy(isLoading = playlists.isEmpty(), loadFailed = false) }
        playlistsJob = observePlaylists()
            .onEach { playlists -> setState { copy(playlists = playlists, isLoading = false) } }
            .catch { setState { copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
        startObservingFavorite()
    }

    private fun stopObserving() {
        playlistsJob?.cancel()
        playlistsJob = null
        favoriteJob?.cancel()
        favoriteJob = null
    }
}
