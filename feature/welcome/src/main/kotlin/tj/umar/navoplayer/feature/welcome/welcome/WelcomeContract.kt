package tj.umar.navoplayer.feature.welcome.welcome

import androidx.compose.runtime.Immutable

internal enum class WelcomePermissionStatus { NotRequested, Denied, PermanentlyDenied }

@Immutable
internal data class WelcomeState(
    val permission: WelcomePermissionStatus = WelcomePermissionStatus.NotRequested,
)

internal sealed interface WelcomeIntent {
    data class ScreenStarted(val granted: Boolean) : WelcomeIntent
    data object GrantAccessClicked : WelcomeIntent
    data class PermissionResult(
        val granted: Boolean,
        val rationaleBefore: Boolean,
        val rationaleAfter: Boolean,
    ) : WelcomeIntent
}

internal sealed interface WelcomeEffect {
    data object RequestAudioPermission : WelcomeEffect
    data object OpenAppSettings : WelcomeEffect
    data object NavigateToLibrary : WelcomeEffect
}
