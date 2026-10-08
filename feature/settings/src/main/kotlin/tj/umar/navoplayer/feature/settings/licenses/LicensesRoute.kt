package tj.umar.navoplayer.feature.settings.licenses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.umar.navoplayer.core.ui.mvi.CollectEffects

@Composable
internal fun LicensesRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicensesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            LicensesEffect.NavigateBack -> currentOnBack()
        }
    }

    LicensesScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
