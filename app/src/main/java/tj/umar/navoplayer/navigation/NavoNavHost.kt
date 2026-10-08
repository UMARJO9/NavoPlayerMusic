package tj.umar.navoplayer.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import tj.umar.navoplayer.feature.library.navigation.GroupDetailDestination
import tj.umar.navoplayer.feature.library.navigation.LibraryDestination
import tj.umar.navoplayer.feature.library.navigation.groupDetailScreen
import tj.umar.navoplayer.feature.library.navigation.libraryScreen
import tj.umar.navoplayer.feature.library.navigation.navigateToGroupDetail
import tj.umar.navoplayer.feature.library.navigation.navigateToLibrary
import tj.umar.navoplayer.feature.player.navigation.NowPlayingDestination
import tj.umar.navoplayer.feature.player.navigation.nowPlayingScreen
import tj.umar.navoplayer.feature.playlists.navigation.PlaylistDetailDestination
import tj.umar.navoplayer.feature.playlists.navigation.navigateToPlaylistDetail
import tj.umar.navoplayer.feature.playlists.navigation.playlistDetailScreen
import tj.umar.navoplayer.feature.search.navigation.SearchDestination
import tj.umar.navoplayer.feature.search.navigation.navigateToSearch
import tj.umar.navoplayer.feature.search.navigation.searchScreen
import tj.umar.navoplayer.feature.welcome.navigation.WelcomeDestination
import tj.umar.navoplayer.feature.welcome.navigation.navigateToWelcome
import tj.umar.navoplayer.feature.welcome.navigation.welcomeScreen

private const val ENTER_FADE_MILLIS = 300
private const val EXIT_FADE_MILLIS = 200

@Composable
fun NavoNavHost(
    navController: NavHostController,
    startDestination: Any,
    onAddToPlaylist: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isResumed = { navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED }
    val onAudioPermissionMissing = {
        navController.navigateToWelcome(
            navOptions {
                popUpTo<LibraryDestination> { inclusive = true }
                launchSingleTop = true
            },
        )
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(ENTER_FADE_MILLIS)) },
        exitTransition = {
            if (targetState.destination.hasRoute<NowPlayingDestination>()) {
                ExitTransition.KeepUntilTransitionsFinished
            } else {
                fadeOut(tween(EXIT_FADE_MILLIS))
            }
        },
        popEnterTransition = {
            if (initialState.destination.hasRoute<NowPlayingDestination>()) {
                EnterTransition.None
            } else {
                fadeIn(tween(ENTER_FADE_MILLIS))
            }
        },
        popExitTransition = { fadeOut(tween(EXIT_FADE_MILLIS)) },
    ) {
        welcomeScreen(
            onPermissionGranted = {
                navController.navigateToLibrary(
                    navOptions {
                        popUpTo<WelcomeDestination> { inclusive = true }
                        launchSingleTop = true
                    },
                )
            },
        )
        libraryScreen(
            onAudioPermissionMissing = onAudioPermissionMissing,
            onGroupClick = { key -> if (isResumed()) navController.navigateToGroupDetail(key) },
            onSearchClick = { if (isResumed()) navController.navigateToSearch() },
            onPlaylistClick = { id -> if (isResumed()) navController.navigateToPlaylistDetail(id) },
            onAddToPlaylist = onAddToPlaylist,
        )
        searchScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<SearchDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onGroupClick = { key -> if (isResumed()) navController.navigateToGroupDetail(key) },
            onAudioPermissionMissing = onAudioPermissionMissing,
            onAddToPlaylist = onAddToPlaylist,
        )
        groupDetailScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<GroupDetailDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onAudioPermissionMissing = onAudioPermissionMissing,
            onAddToPlaylist = onAddToPlaylist,
        )
        playlistDetailScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<PlaylistDetailDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onAudioPermissionMissing = onAudioPermissionMissing,
        )
        nowPlayingScreen(
            onCollapse = {
                if (navController.currentDestination?.hasRoute<NowPlayingDestination>() == true) {
                    navController.popBackStack()
                }
            },
        )
    }
}
