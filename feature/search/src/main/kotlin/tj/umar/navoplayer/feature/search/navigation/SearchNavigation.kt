package tj.umar.navoplayer.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.feature.search.search.SearchRoute

@Serializable
data object SearchDestination

fun NavController.navigateToSearch(navOptions: NavOptions? = null) {
    navigate(SearchDestination, navOptions)
}

fun NavGraphBuilder.searchScreen(
    onBack: () -> Unit,
    onGroupClick: (TrackGroupKey) -> Unit,
    onAudioPermissionMissing: () -> Unit,
    onAddToPlaylist: (List<Long>) -> Unit,
) {
    composable<SearchDestination> {
        SearchRoute(
            onBack = onBack,
            onGroupClick = onGroupClick,
            onAudioPermissionMissing = onAudioPermissionMissing,
            onAddToPlaylist = onAddToPlaylist,
        )
    }
}
