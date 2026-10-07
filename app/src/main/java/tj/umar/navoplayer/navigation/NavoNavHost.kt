package tj.umar.navoplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import tj.umar.navoplayer.feature.library.navigation.LibraryDestination
import tj.umar.navoplayer.feature.library.navigation.libraryScreen

@Composable
fun NavoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LibraryDestination,
        modifier = modifier,
    ) {
        libraryScreen()
    }
}
