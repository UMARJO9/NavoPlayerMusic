package tj.umar.navoplayer.feature.settings.licenses

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.feature.settings.R

@Immutable
internal data class LicenseEntry(
    @param:StringRes val nameRes: Int,
    @param:StringRes val licenseRes: Int,
)

private fun apache(@StringRes nameRes: Int) = LicenseEntry(nameRes, R.string.settings_license_apache)

private fun ofl(@StringRes nameRes: Int) = LicenseEntry(nameRes, R.string.settings_license_ofl)

internal val OpenSourceLibraries: List<LicenseEntry> = listOf(
    apache(R.string.settings_library_kotlin),
    apache(R.string.settings_library_coroutines),
    apache(R.string.settings_library_serialization),
    apache(R.string.settings_library_androidx_core),
    apache(R.string.settings_library_activity),
    apache(R.string.settings_library_lifecycle),
    apache(R.string.settings_library_navigation),
    apache(R.string.settings_library_compose),
    apache(R.string.settings_library_material3),
    apache(R.string.settings_library_media3),
    apache(R.string.settings_library_room),
    apache(R.string.settings_library_datastore),
    apache(R.string.settings_library_hilt),
    apache(R.string.settings_library_guava),
    apache(R.string.settings_library_javax_inject),
    ofl(R.string.settings_library_unbounded),
    ofl(R.string.settings_library_golos),
)
