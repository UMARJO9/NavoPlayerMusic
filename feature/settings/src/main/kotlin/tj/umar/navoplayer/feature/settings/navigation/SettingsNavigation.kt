package tj.umar.navoplayer.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tj.umar.navoplayer.feature.settings.folders.HiddenFoldersRoute
import tj.umar.navoplayer.feature.settings.licenses.LicensesRoute
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

@Serializable
data object HiddenFoldersDestination

fun NavController.navigateToHiddenFolders(navOptions: NavOptions? = null) {
    navigate(HiddenFoldersDestination, navOptions)
}

fun NavGraphBuilder.hiddenFoldersScreen(
    onBack: () -> Unit,
    onAudioPermissionMissing: () -> Unit,
) {
    composable<HiddenFoldersDestination> {
        HiddenFoldersRoute(onBack = onBack, onAudioPermissionMissing = onAudioPermissionMissing)
    }
}

@Serializable
data object LicensesDestination

fun NavController.navigateToLicenses(navOptions: NavOptions? = null) {
    navigate(LicensesDestination, navOptions)
}

fun NavGraphBuilder.licensesScreen(onBack: () -> Unit) {
    composable<LicensesDestination> {
        LicensesRoute(onBack = onBack)
    }
}
