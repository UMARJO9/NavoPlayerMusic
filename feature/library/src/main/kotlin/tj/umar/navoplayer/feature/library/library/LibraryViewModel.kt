package tj.umar.navoplayer.feature.library.library

import dagger.hilt.android.lifecycle.HiltViewModel
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor() :
    MviViewModel<LibraryState, LibraryIntent, LibraryEffect>(LibraryState()) {

    override fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.TabSelected -> selectTab(intent.tab)
        }
    }

    private fun selectTab(tab: LibraryTab) {
        if (tab == currentState.selectedTab) return
        setState { copy(selectedTab = tab) }
    }
}
