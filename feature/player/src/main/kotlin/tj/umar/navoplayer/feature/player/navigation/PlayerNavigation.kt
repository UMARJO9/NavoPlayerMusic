package tj.umar.navoplayer.feature.player.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.feature.player.nowplaying.NowPlayingRoute
import tj.umar.navoplayer.feature.player.queue.QueueRoute

private const val SLIDE_IN_MILLIS = 320
private const val SLIDE_OUT_MILLIS = 260

@Serializable
data object NowPlayingDestination

@Serializable
data object QueueDestination

fun NavController.navigateToNowPlaying() {
    navigate(NowPlayingDestination) { launchSingleTop = true }
}

fun NavController.navigateToQueue() {
    navigate(QueueDestination) { launchSingleTop = true }
}

fun NavGraphBuilder.nowPlayingScreen(onCollapse: () -> Unit, onOpenQueue: () -> Unit) {
    composable<NowPlayingDestination>(
        enterTransition = { slideInVertically(tween(SLIDE_IN_MILLIS)) { it } },
        exitTransition = {
            if (targetState.destination.hasRoute<QueueDestination>()) {
                ExitTransition.KeepUntilTransitionsFinished
            } else {
                slideOutVertically(tween(SLIDE_OUT_MILLIS)) { it }
            }
        },
        popEnterTransition = {
            if (initialState.destination.hasRoute<QueueDestination>()) {
                EnterTransition.None
            } else {
                slideInVertically(tween(SLIDE_IN_MILLIS)) { it }
            }
        },
        popExitTransition = { slideOutVertically(tween(SLIDE_OUT_MILLIS)) { it } },
    ) {
        NowPlayingRoute(onCollapse = onCollapse, onOpenQueue = onOpenQueue)
    }
}

fun NavGraphBuilder.queueScreen(onClose: () -> Unit) {
    composable<QueueDestination>(
        enterTransition = { slideInVertically(tween(SLIDE_IN_MILLIS)) { it } },
        exitTransition = { slideOutVertically(tween(SLIDE_OUT_MILLIS)) { it } },
        popEnterTransition = { slideInVertically(tween(SLIDE_IN_MILLIS)) { it } },
        popExitTransition = { slideOutVertically(tween(SLIDE_OUT_MILLIS)) { it } },
    ) {
        QueueRoute(onClose = onClose)
    }
}
