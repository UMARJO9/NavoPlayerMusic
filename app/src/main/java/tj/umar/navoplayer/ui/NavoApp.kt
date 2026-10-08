package tj.umar.navoplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.ui.permission.hasAudioReadPermission
import tj.umar.navoplayer.feature.library.navigation.LibraryDestination
import tj.umar.navoplayer.feature.welcome.navigation.WelcomeDestination
import tj.umar.navoplayer.navigation.NavoNavHost

@Composable
fun NavoApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val startOnWelcome = rememberSaveable { !context.hasAudioReadPermission() }
    NavoNavHost(
        navController = navController,
        startDestination = if (startOnWelcome) WelcomeDestination else LibraryDestination,
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background),
    )
}
