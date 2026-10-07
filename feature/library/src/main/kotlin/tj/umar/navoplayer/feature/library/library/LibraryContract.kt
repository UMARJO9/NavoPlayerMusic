package tj.umar.navoplayer.feature.library.library

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.feature.library.R

internal enum class LibraryTab(@param:StringRes val titleRes: Int) {
    Tracks(R.string.library_tab_tracks),
    Albums(R.string.library_tab_albums),
    Artists(R.string.library_tab_artists),
    Folders(R.string.library_tab_folders),
}

@Immutable
internal data class LibraryState(
    val selectedTab: LibraryTab = LibraryTab.Tracks,
)

internal sealed interface LibraryIntent {
    data class TabSelected(val tab: LibraryTab) : LibraryIntent
}

internal sealed interface LibraryEffect
