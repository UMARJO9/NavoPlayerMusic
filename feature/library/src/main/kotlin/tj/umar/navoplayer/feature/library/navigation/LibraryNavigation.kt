package tj.umar.navoplayer.feature.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.feature.library.groupdetail.GroupDetailRoute
import tj.umar.navoplayer.feature.library.library.LibraryRoute

private const val NO_ID = -1L

@Serializable
data object LibraryDestination

@Serializable
enum class GroupDetailType { Album, Artist, Folder }

@Serializable
data class GroupDetailDestination(
    val type: GroupDetailType,
    val id: Long = NO_ID,
    val name: String? = null,
)

fun NavController.navigateToLibrary(navOptions: NavOptions? = null) {
    navigate(LibraryDestination, navOptions)
}

fun NavController.navigateToGroupDetail(key: TrackGroupKey) {
    navigate(key.toDestination())
}

fun NavGraphBuilder.libraryScreen(
    onAudioPermissionMissing: () -> Unit,
    onGroupClick: (TrackGroupKey) -> Unit,
) {
    composable<LibraryDestination> {
        LibraryRoute(onAudioPermissionMissing = onAudioPermissionMissing, onGroupClick = onGroupClick)
    }
}

fun NavGraphBuilder.groupDetailScreen(
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
) {
    composable<GroupDetailDestination> { entry ->
        GroupDetailRoute(
            key = entry.toRoute<GroupDetailDestination>().toKey(),
            onBack = onBack,
            onAudioPermissionMissing = onAudioPermissionMissing,
        )
    }
}

fun NavDestination.showsMiniPlayer(): Boolean =
    hasRoute<LibraryDestination>() || hasRoute<GroupDetailDestination>()

internal fun GroupDetailDestination.toKey(): TrackGroupKey = TrackGroupKey(
    type = when (type) {
        GroupDetailType.Album -> TrackGroupType.Album
        GroupDetailType.Artist -> TrackGroupType.Artist
        GroupDetailType.Folder -> TrackGroupType.Folder
    },
    id = id.takeIf { it != NO_ID },
    name = name,
)

internal fun TrackGroupKey.toDestination(): GroupDetailDestination = GroupDetailDestination(
    type = when (type) {
        TrackGroupType.Album -> GroupDetailType.Album
        TrackGroupType.Artist -> GroupDetailType.Artist
        TrackGroupType.Folder -> GroupDetailType.Folder
    },
    id = id ?: NO_ID,
    name = name,
)
