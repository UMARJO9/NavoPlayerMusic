package tj.umar.navoplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.core.domain.usecase.ObserveShowCoverArtUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveThemeModeUseCase
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase,
    observeShowCoverArt: ObserveShowCoverArtUseCase,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = combine(observeThemeMode(), observeShowCoverArt()) { themeMode, showCoverArt ->
        MainActivityUiState.Ready(themeMode, showCoverArt)
    }
        .catch<MainActivityUiState> { emit(MainActivityUiState.Ready(ThemeMode.Default)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MainActivityUiState.Loading)
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Ready(
        val themeMode: ThemeMode,
        val showCoverArt: Boolean = true,
    ) : MainActivityUiState
}
