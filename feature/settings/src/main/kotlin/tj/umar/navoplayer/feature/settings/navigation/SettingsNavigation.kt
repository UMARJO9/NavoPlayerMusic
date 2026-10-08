package tj.umar.navoplayer.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.feature.settings.settings.SettingsRoute

@Serializable
data object SettingsDestination

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    navigate(SettingsDestination, navOptions)
}

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onHiddenFoldersClick: () -> Unit,
    onLicensesClick: () -> Unit,
) {
    composable<SettingsDestination> {
        SettingsRoute(
            onBack = onBack,
            onHiddenFoldersClick = onHiddenFoldersClick,
            onLicensesClick = onLicensesClick,
        )
    }
}
