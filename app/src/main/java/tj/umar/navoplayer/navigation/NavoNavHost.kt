package tj.umar.navoplayer.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import tj.umar.navoplayer.feature.library.navigation.LibraryDestination
import tj.umar.navoplayer.feature.library.navigation.navigateToLibrary
import tj.umar.navoplayer.feature.library.navigation.libraryScreen
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
        exitTransition = { fadeOut(tween(EXIT_FADE_MILLIS)) },
        popEnterTransition = { fadeIn(tween(ENTER_FADE_MILLIS)) },
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
        )
    }
}
