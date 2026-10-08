package tj.umar.navoplayer.feature.settings.licenses

import androidx.compose.runtime.Immutable

@Immutable
internal data class LicensesState(
    val entries: List<LicenseEntry> = OpenSourceLibraries,
)

internal sealed interface LicensesIntent {
    data object BackClicked : LicensesIntent
}

internal sealed interface LicensesEffect {
    data object NavigateBack : LicensesEffect
}
