package tj.umar.navoplayer.feature.welcome.welcome

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class WelcomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : MviViewModel<WelcomeState, WelcomeIntent, WelcomeEffect>(
    WelcomeState(permission = savedStateHandle.restoredPermission()),
) {

    private var deniedWithoutRationale: Boolean
        get() = savedStateHandle[KEY_DENIED_WITHOUT_RATIONALE] ?: false
        set(value) {
            savedStateHandle[KEY_DENIED_WITHOUT_RATIONALE] = value
        }

    override fun onIntent(intent: WelcomeIntent) {
        when (intent) {
            is WelcomeIntent.ScreenStarted -> if (intent.granted) sendEffect(WelcomeEffect.NavigateToLibrary)
            WelcomeIntent.GrantAccessClicked -> onGrantAccessClicked()
            is WelcomeIntent.PermissionResult -> onPermissionResult(intent)
        }
    }

    private fun onGrantAccessClicked() {
        val effect = if (currentState.permission == WelcomePermissionStatus.PermanentlyDenied) {
            WelcomeEffect.OpenAppSettings
        } else {
            WelcomeEffect.RequestAudioPermission
        }
        sendEffect(effect)
    }

    private fun onPermissionResult(result: WelcomeIntent.PermissionResult) {
        when {
            result.granted -> {
                deniedWithoutRationale = false
                sendEffect(WelcomeEffect.NavigateToLibrary)
            }
            result.rationaleAfter -> {
                deniedWithoutRationale = false
                updatePermission(WelcomePermissionStatus.Denied)
            }
            result.rationaleBefore || deniedWithoutRationale ->
                updatePermission(WelcomePermissionStatus.PermanentlyDenied)
            else -> {
                deniedWithoutRationale = true
                updatePermission(WelcomePermissionStatus.Denied)
            }
        }
    }

    private fun updatePermission(status: WelcomePermissionStatus) {
        savedStateHandle[KEY_PERMISSION] = status.name
        setState { copy(permission = status) }
    }
}

private const val KEY_PERMISSION = "welcome_permission"
private const val KEY_DENIED_WITHOUT_RATIONALE = "welcome_denied_without_rationale"

private fun SavedStateHandle.restoredPermission(): WelcomePermissionStatus =
    get<String>(KEY_PERMISSION)
        ?.let { name -> WelcomePermissionStatus.entries.firstOrNull { it.name == name } }
        ?: WelcomePermissionStatus.NotRequested
