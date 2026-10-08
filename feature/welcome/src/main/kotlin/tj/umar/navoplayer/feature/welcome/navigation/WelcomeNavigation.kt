package tj.umar.navoplayer.feature.welcome.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.feature.welcome.welcome.WelcomeRoute

@Serializable
data object WelcomeDestination

fun NavController.navigateToWelcome(navOptions: NavOptions? = null) {
    navigate(WelcomeDestination, navOptions)
}

fun NavGraphBuilder.welcomeScreen(onPermissionGranted: () -> Unit) {
    composable<WelcomeDestination> {
        WelcomeRoute(onPermissionGranted = onPermissionGranted)
    }
}
