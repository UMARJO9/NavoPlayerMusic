package tj.umar.navoplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.core.domain.usecase.ObserveThemeModeUseCase
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = observeThemeMode()
        .map<ThemeMode, MainActivityUiState> { MainActivityUiState.Ready(it) }
        .catch { emit(MainActivityUiState.Ready(ThemeMode.Default)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MainActivityUiState.Loading)
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Ready(val themeMode: ThemeMode) : MainActivityUiState
}
