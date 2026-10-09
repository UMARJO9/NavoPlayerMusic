package tj.umar.navoplayer.feature.settings.settings

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.MinTrackDuration

@Immutable
internal data class SettingsState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val minTrackDuration: MinTrackDuration = MinTrackDuration.Off,
    val hiddenFolderCount: Int = 0,
    val pauseOnHeadphonesDisconnect: Boolean = true,
    val versionName: String = "",
    val equalizerSummary: EqualizerSummary = EqualizerSummary.Loading,
) {
    val durationOptions: List<MinTrackDuration>
        get() = MinTrackDuration.entries
}

internal sealed interface SettingsIntent {
    data object ScreenStarted : SettingsIntent
    data object ScreenStopped : SettingsIntent
    data object RetryLoad : SettingsIntent
    data class MinTrackDurationSelected(val value: MinTrackDuration) : SettingsIntent
    data class PauseOnHeadphonesDisconnectToggled(val enabled: Boolean) : SettingsIntent
    data object HiddenFoldersClicked : SettingsIntent
    data object LicensesClicked : SettingsIntent
    data object EqualizerClicked : SettingsIntent
    data object BackClicked : SettingsIntent
}

internal sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
    data object NavigateToHiddenFolders : SettingsEffect
    data object NavigateToLicenses : SettingsEffect
    data object NavigateToEqualizer : SettingsEffect
    data object ShowSaveFailed : SettingsEffect
}

internal sealed interface EqualizerSummary {
    data object Loading : EqualizerSummary
    data object Off : EqualizerSummary
    data object Custom : EqualizerSummary
    data object Unsupported : EqualizerSummary
    data class Preset(val name: String) : EqualizerSummary
}
