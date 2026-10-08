package tj.umar.navoplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission
import tj.umar.navoplayer.feature.library.navigation.LibraryDestination
import tj.umar.navoplayer.feature.player.miniplayer.MiniPlayerRoute
import tj.umar.navoplayer.feature.player.navigation.navigateToNowPlaying
import tj.umar.navoplayer.feature.playlists.addto.AddToPlaylistRequest
import tj.umar.navoplayer.feature.playlists.addto.AddToPlaylistSheetRoute
import tj.umar.navoplayer.feature.welcome.navigation.WelcomeDestination
import tj.umar.navoplayer.navigation.NavoNavHost
import tj.umar.navoplayer.navigation.showsMiniPlayer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavoApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val startOnWelcome = rememberSaveable { !context.hasAudioReadPermission() }
    val currentEntry by navController.currentBackStackEntryAsState()
    val imeVisible = WindowInsets.isImeVisible
    val showMiniPlayer = currentEntry?.destination?.showsMiniPlayer() == true && !imeVisible
    var addToPlaylistRequest by rememberSaveable(stateSaver = AddToPlaylistRequest.Saver) {
        mutableStateOf<AddToPlaylistRequest?>(null)
    }
    val isOnWelcome = currentEntry?.destination?.hasRoute<WelcomeDestination>() == true
    LaunchedEffect(isOnWelcome) {
        if (isOnWelcome) addToPlaylistRequest = null
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background),
    ) {
        NavoNavHost(
            navController = navController,
            startDestination = if (startOnWelcome) WelcomeDestination else LibraryDestination,
            onAddToPlaylist = { trackIds -> addToPlaylistRequest = AddToPlaylistRequest.of(trackIds) },
            modifier = Modifier.fillMaxSize(),
        )
        MiniPlayerRoute(
            visible = showMiniPlayer,
            onOpenNowPlaying = navController::navigateToNowPlaying,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        addToPlaylistRequest?.let { request ->
            AddToPlaylistSheetRoute(
                request = request,
                onDismiss = { addToPlaylistRequest = null },
            )
        }
    }
}
