package tj.umar.navoplayer.feature.settings.folders

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.LibraryFolder

@Immutable
internal data class HiddenFoldersState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val folders: List<LibraryFolder> = emptyList(),
)

internal sealed interface HiddenFoldersIntent {
    data class ScreenStarted(val hasPermission: Boolean) : HiddenFoldersIntent
    data object ScreenStopped : HiddenFoldersIntent
    data object RetryLoad : HiddenFoldersIntent
    data class FolderVisibilityToggled(val path: String, val visible: Boolean) : HiddenFoldersIntent
    data object BackClicked : HiddenFoldersIntent
}

internal sealed interface HiddenFoldersEffect {
    data object NavigateBack : HiddenFoldersEffect
    data object NavigateToWelcome : HiddenFoldersEffect
    data object ShowSaveFailed : HiddenFoldersEffect
}
