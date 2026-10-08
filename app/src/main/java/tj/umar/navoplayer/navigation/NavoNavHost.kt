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
import tj.umar.navoplayer.feature.library.navigation.navigateToGroupDetail
import tj.umar.navoplayer.feature.library.navigation.navigateToLibrary
import tj.umar.navoplayer.feature.library.navigation.libraryScreen
import tj.umar.navoplayer.feature.player.navigation.NowPlayingDestination
import tj.umar.navoplayer.feature.player.navigation.nowPlayingScreen
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
    modifier: Modifier = Modifier,
) {
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
            onAudioPermissionMissing = {
                navController.navigateToWelcome(
                    navOptions {
                        popUpTo<LibraryDestination> { inclusive = true }
                        launchSingleTop = true
                    },
                )
            },
            onGroupClick = { key ->
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.navigateToGroupDetail(key)
                }
            },
            onSearchClick = {
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.navigateToSearch()
                }
            },
        )
        searchScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<SearchDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onGroupClick = { key ->
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.navigateToGroupDetail(key)
                }
            },
            onAudioPermissionMissing = {
                navController.navigateToWelcome(
                    navOptions {
                        popUpTo<LibraryDestination> { inclusive = true }
                        launchSingleTop = true
                    },
                )
            },
        )
        groupDetailScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<GroupDetailDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onAudioPermissionMissing = {
                navController.navigateToWelcome(
                    navOptions {
                        popUpTo<LibraryDestination> { inclusive = true }
                        launchSingleTop = true
                    },
                )
            },
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
