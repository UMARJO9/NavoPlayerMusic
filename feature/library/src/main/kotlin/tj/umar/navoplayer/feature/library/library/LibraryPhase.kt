package tj.umar.navoplayer.feature.library.library

import tj.umar.navoplayer.feature.library.component.ContentPhase
import tj.umar.navoplayer.feature.library.component.contentPhase

internal fun LibraryState.contentPhase(): ContentPhase =
    contentPhase(hasContent = tracks.isNotEmpty(), isLoading = isLoadingTracks, loadFailed = tracksLoadFailed)
