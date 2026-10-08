package tj.umar.navoplayer.feature.settings.licenses

import dagger.hilt.android.lifecycle.HiltViewModel
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class LicensesViewModel @Inject constructor() :
    MviViewModel<LicensesState, LicensesIntent, LicensesEffect>(LicensesState()) {

    override fun onIntent(intent: LicensesIntent) {
        when (intent) {
            LicensesIntent.BackClicked -> sendEffect(LicensesEffect.NavigateBack)
        }
    }
}
