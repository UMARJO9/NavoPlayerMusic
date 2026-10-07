package tj.umar.navoplayer.feature.library.library

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.feature.library.R

internal enum class LibraryTab(@param:StringRes val titleRes: Int) {
    Tracks(R.string.library_tab_tracks),
    Albums(R.string.library_tab_albums),
    Artists(R.string.library_tab_artists),
    Folders(R.string.library_tab_folders),
}

internal enum class AudioPermissionStatus { Unknown, Granted, Denied, PermanentlyDenied }

@Immutable
internal data class LibraryState(
    val selectedTab: LibraryTab = LibraryTab.Tracks,
    val audioPermission: AudioPermissionStatus = AudioPermissionStatus.Unknown,
    val isLoadingTracks: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val tracksLoadFailed: Boolean = false,
)

internal sealed interface LibraryIntent {
    data class TabSelected(val tab: LibraryTab) : LibraryIntent
    data class PermissionChecked(val granted: Boolean) : LibraryIntent
    data class PermissionResult(
        val granted: Boolean,
        val rationaleBefore: Boolean,
        val rationaleAfter: Boolean,
    ) : LibraryIntent
    data object GrantPermissionClicked : LibraryIntent
    data object RetryLoadTracks : LibraryIntent
    data object ScreenStopped : LibraryIntent
}

internal sealed interface LibraryEffect {
    data object RequestAudioPermission : LibraryEffect
    data object OpenAppSettings : LibraryEffect
}
