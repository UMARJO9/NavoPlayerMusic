package tj.umar.navoplayer.feature.player.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.feature.player.nowplaying.NowPlayingRoute

private const val SLIDE_IN_MILLIS = 320
private const val SLIDE_OUT_MILLIS = 260

@Serializable
data object NowPlayingDestination

fun NavController.navigateToNowPlaying() {
    navigate(NowPlayingDestination) { launchSingleTop = true }
}

fun NavGraphBuilder.nowPlayingScreen(onCollapse: () -> Unit) {
    composable<NowPlayingDestination>(
        enterTransition = { slideInVertically(tween(SLIDE_IN_MILLIS)) { it } },
        exitTransition = { slideOutVertically(tween(SLIDE_OUT_MILLIS)) { it } },
        popEnterTransition = { slideInVertically(tween(SLIDE_IN_MILLIS)) { it } },
        popExitTransition = { slideOutVertically(tween(SLIDE_OUT_MILLIS)) { it } },
    ) {
        NowPlayingRoute(onCollapse = onCollapse)
    }
}
