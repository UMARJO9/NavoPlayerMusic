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
import tj.umar.navoplayer.feature.player.navigation.QueueDestination
import tj.umar.navoplayer.feature.player.navigation.navigateToQueue
import tj.umar.navoplayer.feature.player.navigation.nowPlayingScreen
import tj.umar.navoplayer.feature.player.navigation.queueScreen
import tj.umar.navoplayer.feature.playlists.navigation.FavoritesDestination
import tj.umar.navoplayer.feature.playlists.navigation.favoritesScreen
import tj.umar.navoplayer.feature.playlists.navigation.navigateToFavorites
import tj.umar.navoplayer.feature.playlists.navigation.PlaylistDetailDestination
import tj.umar.navoplayer.feature.playlists.navigation.navigateToPlaylistDetail
import tj.umar.navoplayer.feature.playlists.navigation.playlistDetailScreen
import tj.umar.navoplayer.feature.search.navigation.SearchDestination
import tj.umar.navoplayer.feature.search.navigation.navigateToSearch
import tj.umar.navoplayer.feature.search.navigation.searchScreen
import tj.umar.navoplayer.feature.settings.navigation.EqualizerDestination
import tj.umar.navoplayer.feature.settings.navigation.HiddenFoldersDestination
import tj.umar.navoplayer.feature.settings.navigation.LicensesDestination
import tj.umar.navoplayer.feature.settings.navigation.SettingsDestination
import tj.umar.navoplayer.feature.settings.navigation.equalizerScreen
import tj.umar.navoplayer.feature.settings.navigation.hiddenFoldersScreen
import tj.umar.navoplayer.feature.settings.navigation.licensesScreen
import tj.umar.navoplayer.feature.settings.navigation.navigateToEqualizer
import tj.umar.navoplayer.feature.settings.navigation.navigateToHiddenFolders
import tj.umar.navoplayer.feature.settings.navigation.navigateToLicenses
import tj.umar.navoplayer.feature.settings.navigation.navigateToSettings
import tj.umar.navoplayer.feature.settings.navigation.settingsScreen
import tj.umar.navoplayer.feature.welcome.navigation.WelcomeDestination
import tj.umar.navoplayer.feature.welcome.navigation.navigateToWelcome
import tj.umar.navoplayer.feature.welcome.navigation.welcomeScreen

private const val ENTER_FADE_MILLIS = 300
private const val EXIT_FADE_MILLIS = 200

@Composable
fun NavoNavHost(
    navController: NavHostController,
    startDestination: Any,
    onTrackActions: (List<Long>) -> Unit,
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
            onFavoritesClick = { if (isResumed()) navController.navigateToFavorites() },
            onSettingsClick = { if (isResumed()) navController.navigateToSettings() },
            onTrackActions = onTrackActions,
        )
        searchScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<SearchDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onGroupClick = { key -> if (isResumed()) navController.navigateToGroupDetail(key) },
            onAudioPermissionMissing = onAudioPermissionMissing,
            onTrackActions = onTrackActions,
        )
        groupDetailScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<GroupDetailDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onAudioPermissionMissing = onAudioPermissionMissing,
            onTrackActions = onTrackActions,
        )
        playlistDetailScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<PlaylistDetailDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onAudioPermissionMissing = onAudioPermissionMissing,
        )
        favoritesScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<FavoritesDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onAudioPermissionMissing = onAudioPermissionMissing,
        )
        settingsScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<SettingsDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onHiddenFoldersClick = { if (isResumed()) navController.navigateToHiddenFolders() },
            onLicensesClick = { if (isResumed()) navController.navigateToLicenses() },
            onEqualizerClick = { if (isResumed()) navController.navigateToEqualizer() },
        )
        hiddenFoldersScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<HiddenFoldersDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onAudioPermissionMissing = onAudioPermissionMissing,
        )
        equalizerScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<EqualizerDestination>() == true) {
                    navController.popBackStack()
                }
            },
        )
        licensesScreen(
            onBack = {
                if (navController.currentDestination?.hasRoute<LicensesDestination>() == true) {
                    navController.popBackStack()
                }
            },
        )
        nowPlayingScreen(
            onCollapse = {
                if (navController.currentDestination?.hasRoute<NowPlayingDestination>() == true) {
                    navController.popBackStack()
                }
            },
            onOpenQueue = { if (isResumed()) navController.navigateToQueue() },
        )
        queueScreen(
            onClose = {
                if (navController.currentDestination?.hasRoute<QueueDestination>() == true) {
                    navController.popBackStack()
                }
            },
        )
    }
}
