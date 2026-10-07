package tj.umar.navoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tj.umar.navoplayer.navigation.NavoNavHost

@Composable
fun NavoApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavoNavHost(
        navController = navController,
        modifier = modifier,
    )
}
