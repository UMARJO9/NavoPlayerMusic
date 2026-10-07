package tj.umar.navoplayer.feature.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.feature.library.library.LibraryRoute

@Serializable
data object LibraryDestination

fun NavController.navigateToLibrary(navOptions: NavOptions? = null) {
    navigate(LibraryDestination, navOptions)
}

fun NavGraphBuilder.libraryScreen() {
    composable<LibraryDestination> {
        LibraryRoute()
    }
}
