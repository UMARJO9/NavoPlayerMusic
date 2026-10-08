package tj.umar.navoplayer.feature.playlists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.feature.playlists.detail.PlaylistDetailRoute
import tj.umar.navoplayer.feature.playlists.favorites.FavoritesRoute

@Serializable
data class PlaylistDetailDestination(val playlistId: Long)

fun NavController.navigateToPlaylistDetail(playlistId: Long, navOptions: NavOptions? = null) {
    navigate(PlaylistDetailDestination(playlistId), navOptions)
}

fun NavGraphBuilder.playlistDetailScreen(
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
) {
    composable<PlaylistDetailDestination> { entry ->
        PlaylistDetailRoute(
            playlistId = entry.toRoute<PlaylistDetailDestination>().playlistId,
            onBack = onBack,
            onAudioPermissionMissing = onAudioPermissionMissing,
        )
    }
}

@Serializable
data object FavoritesDestination

fun NavController.navigateToFavorites(navOptions: NavOptions? = null) {
    navigate(FavoritesDestination, navOptions)
}

fun NavGraphBuilder.favoritesScreen(
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
) {
    composable<FavoritesDestination> {
        FavoritesRoute(
            onBack = onBack,
            onAudioPermissionMissing = onAudioPermissionMissing,
        )
    }
}
