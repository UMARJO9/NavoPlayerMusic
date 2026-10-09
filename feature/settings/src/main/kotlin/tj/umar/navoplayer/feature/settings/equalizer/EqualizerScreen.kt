package tj.umar.navoplayer.feature.settings.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.NavoChip
import tj.umar.navoplayer.core.designsystem.component.NavoLevelSlider
import tj.umar.navoplayer.core.designsystem.component.NavoSlider
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.SettingsSectionHeader
import tj.umar.navoplayer.core.designsystem.component.SettingsSwitchRow
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.BASS_BOOST_MAX_STRENGTH
import tj.umar.navoplayer.core.domain.model.EQUALIZER_LEVEL_STEP_MB
import tj.umar.navoplayer.core.domain.model.EqualizerPreset
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.feature.settings.R
import tj.umar.navoplayer.feature.settings.settings.SettingsTopBar
import kotlin.math.roundToInt

private const val PERCENT = 100

@Composable
internal fun EqualizerScreen(
    state: EqualizerState,
    onIntent: (EqualizerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        SettingsTopBar(
            title = stringResource(R.string.equalizer_title),
            onBack = { onIntent(EqualizerIntent.BackClicked) },
        )
        PhasedContent(
            phase = state.phase.toContentPhase(),
            error = {
                StateMessage(
                    message = stringResource(R.string.equalizer_load_error),
                    palette = MedallionPalettes.all[0],
                    actionLabel = stringResource(R.string.settings_folders_retry),
                    onAction = { onIntent(EqualizerIntent.RetryLoad) },
                )
            },
            empty = {
                StateMessage(message = stringResource(R.string.equalizer_unsupported), palette = MedallionPalettes.all[3])
            },
        ) {
            EqualizerContent(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun EqualizerContent(state: EqualizerState, onIntent: (EqualizerIntent) -> Unit) {
    val controlsEnabled = state.controlsEnabled
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = NavoSpacing.Large),
    ) {
        item(key = "enabled") {
            SettingsSwitchRow(
                title = stringResource(R.string.equalizer_enable_title),
                subtitle = stringResource(R.string.equalizer_enable_subtitle),
                checked = state.enabled,
                onCheckedChange = { onIntent(EqualizerIntent.EnabledToggled(it)) },
            )
        }
        item(key = "presets") {
            SettingsSectionHeader(title = stringResource(R.string.equalizer_section_presets))
            PresetChips(state = state, enabled = controlsEnabled, onIntent = onIntent)
        }
        item(key = "bands_header") {
            SettingsSectionHeader(title = stringResource(R.string.equalizer_section_bands))
        }
        items(state.bands, key = { "band_${it.index}" }) { band ->
            BandRow(
                band = band,
                range = state.minLevelMb..state.maxLevelMb,
                enabled = controlsEnabled,
                onIntent = onIntent,
            )
        }
        if (state.bassBoostSupported) {
            item(key = "bass") {
                SettingsSectionHeader(title = stringResource(R.string.equalizer_section_bass))
                val percent = state.bassBoostStrength * PERCENT / BASS_BOOST_MAX_STRENGTH
                NavoSlider(
                    value = state.bassBoostStrength / BASS_BOOST_MAX_STRENGTH.toFloat(),
                    onValueChange = { fraction ->
                        onIntent(EqualizerIntent.BassBoostChanged((fraction * BASS_BOOST_MAX_STRENGTH).roundToInt()))
                    },
                    onValueChangeFinished = { onIntent(EqualizerIntent.BassBoostChangeFinished) },
                    stateDescription = stringResource(R.string.equalizer_bass_value, percent),
                    contentDescription = stringResource(R.string.equalizer_bass_description),
                    enabled = controlsEnabled,
                    modifier = Modifier.padding(horizontal = NavoSpacing.ScreenHorizontal),
                )
            }
        }
        item(key = "reset") {
            NavoButton(
                text = stringResource(R.string.equalizer_reset),
                onClick = { onIntent(EqualizerIntent.ResetClicked) },
                enabled = controlsEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NavoSpacing.ScreenHorizontal, vertical = NavoSpacing.Large),
            )
        }
    }
}

@Composable
private fun PresetChips(state: EqualizerState, enabled: Boolean, onIntent: (EqualizerIntent) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        contentPadding = PaddingValues(horizontal = NavoSpacing.ScreenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small),
    ) {
        items(state.presets, key = { "preset_${it.index}" }) { preset ->
            NavoChip(
                label = preset.name,
                selected = state.selectedPreset == EqualizerPresetSelection.Preset(preset.index),
                onClick = { onIntent(EqualizerIntent.PresetSelected(preset.index)) },
                role = Role.RadioButton,
                enabled = enabled,
            )
        }
        item(key = "custom") {
            NavoChip(
                label = stringResource(R.string.equalizer_preset_custom),
                selected = state.selectedPreset == EqualizerPresetSelection.Custom,
                onClick = { onIntent(EqualizerIntent.CustomSelected) },
                role = Role.RadioButton,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun BandRow(band: EqualizerBandUi, range: IntRange, enabled: Boolean, onIntent: (EqualizerIntent) -> Unit) {
    val colors = NavoTheme.colors
    val frequency = frequencyText(band.centerFrequencyHz)
    val level = stringResource(R.string.equalizer_level_db, formatDecibels(band.levelMb))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.ScreenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small),
    ) {
        Text(
            text = frequency,
            style = NavoTheme.typography.secondaryNumeric,
            color = colors.contentSecondary,
            modifier = Modifier
                .width(64.dp)
                .clearAndSetSemantics { },
        )
        NavoLevelSlider(
            value = band.levelMb,
            valueRange = range,
            step = EQUALIZER_LEVEL_STEP_MB,
            onValueChange = { onIntent(EqualizerIntent.BandLevelChanged(band.index, it)) },
            onValueChangeFinished = { onIntent(EqualizerIntent.BandLevelChangeFinished(band.index)) },
            stateDescription = level,
            contentDescription = stringResource(R.string.equalizer_band_description, frequency),
            enabled = enabled,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = level,
            style = NavoTheme.typography.secondaryNumeric,
            color = if (enabled) colors.content else colors.contentMuted,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(64.dp)
                .clearAndSetSemantics { },
        )
    }
}

@Composable
private fun frequencyText(hz: Int): String = when (val label = frequencyLabel(hz)) {
    is FrequencyLabel.Hertz -> stringResource(R.string.equalizer_frequency_hz, label.value)
    is FrequencyLabel.Kilohertz -> stringResource(R.string.equalizer_frequency_khz, label.value)
}

private fun EqualizerPhase.toContentPhase(): ContentPhase = when (this) {
    EqualizerPhase.Loading -> ContentPhase.Loading
    EqualizerPhase.Ready -> ContentPhase.Content
    EqualizerPhase.Unsupported -> ContentPhase.Empty
    EqualizerPhase.LoadFailed -> ContentPhase.Error
}

private val previewEqualizerState = EqualizerState(
    phase = EqualizerPhase.Ready,
    enabled = true,
    bands = listOf(60, 230, 910, 3_600, 14_000).mapIndexed { index, hz ->
        EqualizerBandUi(index, hz, listOf(500, 300, -100, 300, 500)[index])
    },
    minLevelMb = -1500,
    maxLevelMb = 1500,
    presets = listOf("Normal", "Classical", "Rock").mapIndexed { index, name ->
        EqualizerPreset(index, name, emptyList())
    },
    selectedPreset = EqualizerPresetSelection.Preset(2),
    bassBoostSupported = true,
    bassBoostStrength = 400,
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EqualizerScreenPreview() {
    NavoTheme {
        EqualizerScreen(state = previewEqualizerState, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EqualizerScreenOffPreview() {
    NavoTheme {
        EqualizerScreen(state = previewEqualizerState.copy(enabled = false), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EqualizerScreenUnsupportedPreview() {
    NavoTheme {
        EqualizerScreen(state = EqualizerState(phase = EqualizerPhase.Unsupported), onIntent = {})
    }
}
