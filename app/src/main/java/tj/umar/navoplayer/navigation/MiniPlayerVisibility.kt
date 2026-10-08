package tj.umar.navoplayer.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import tj.umar.navoplayer.feature.library.navigation.GroupDetailDestination
import tj.umar.navoplayer.feature.library.navigation.LibraryDestination
import tj.umar.navoplayer.feature.search.navigation.SearchDestination

internal fun NavDestination.showsMiniPlayer(): Boolean =
    hasRoute<LibraryDestination>() || hasRoute<GroupDetailDestination>() || hasRoute<SearchDestination>()
