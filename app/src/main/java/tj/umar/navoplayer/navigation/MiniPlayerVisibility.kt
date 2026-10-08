package tj.umar.navoplayer.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import tj.umar.navoplayer.feature.library.navigation.GroupDetailDestination
import tj.umar.navoplayer.feature.library.navigation.LibraryDestination

internal fun NavDestination.showsMiniPlayer(): Boolean =
    hasRoute<LibraryDestination>() || hasRoute<GroupDetailDestination>()
