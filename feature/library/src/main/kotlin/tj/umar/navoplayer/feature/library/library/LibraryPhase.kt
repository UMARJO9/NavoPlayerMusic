package tj.umar.navoplayer.feature.library.library

import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.contentPhase

internal fun LibraryState.contentPhase(): ContentPhase =
    contentPhase(hasContent = tracks.isNotEmpty(), isLoading = isLoadingTracks, loadFailed = tracksLoadFailed)
