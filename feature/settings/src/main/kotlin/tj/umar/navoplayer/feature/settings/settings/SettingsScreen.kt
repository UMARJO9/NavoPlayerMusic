package tj.umar.navoplayer.feature.settings.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.NavoChip
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.SettingsGroup
import tj.umar.navoplayer.core.designsystem.component.SettingsGroupDivider
import tj.umar.navoplayer.core.designsystem.component.SettingsGroupDividerInset
import tj.umar.navoplayer.core.designsystem.component.SettingsIconTile
import tj.umar.navoplayer.core.designsystem.component.SettingsInfoRow
import tj.umar.navoplayer.core.designsystem.component.SettingsNavigationRow
import tj.umar.navoplayer.core.designsystem.component.SettingsSectionHeader
import tj.umar.navoplayer.core.designsystem.component.SettingsSwitchRow
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.component.Wordmark
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.OrnamentDivider
import tj.umar.navoplayer.core.designsystem.medallion.khotamWatermark
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.feature.settings.R
import tj.umar.navoplayer.core.designsystem.R as DesignSystemR

private const val HEADER_ITEM_KEY = "header"
private const val WATERMARK_ALPHA = 0.10f
private val FooterMedallionSize = 40.dp
private val CompactTitleThreshold = 96.dp
private val BackButtonSize = 48.dp

@Composable
internal fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = when {
        state.isLoading -> ContentPhase.Loading
        state.loadFailed -> ContentPhase.Error
        else -> ContentPhase.Content
    }
    val listState = rememberLazyListState()
    val thresholdPx = with(LocalDensity.current) { CompactTitleThreshold.toPx() }
    val compactTitleAlpha by remember(listState, thresholdPx) {
        derivedStateOf { listState.compactTitleAlpha(thresholdPx) }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        SettingsCompactBar(
            title = stringResource(R.string.settings_title),
            titleAlpha = { if (phase == ContentPhase.Content) compactTitleAlpha else 1f },
            titleIsHeading = phase != ContentPhase.Content,
            onBack = { onIntent(SettingsIntent.BackClicked) },
        )
        PhasedContent(
            phase = phase,
            modifier = Modifier.fillMaxSize(),
            error = {
                StateMessage(
                    message = stringResource(R.string.settings_load_error),
                    palette = MedallionPalettes.all[0],
                    actionLabel = stringResource(R.string.settings_retry),
                    onAction = { onIntent(SettingsIntent.RetryLoad) },
                )
            },
        ) {
            SettingsContent(state = state, onIntent = onIntent, listState = listState)
        }
    }
}

private fun LazyListState.compactTitleAlpha(thresholdPx: Float): Float = when {
    firstVisibleItemIndex > 0 -> 1f
    thresholdPx <= 0f -> 1f
    else -> (firstVisibleItemScrollOffset / thresholdPx).coerceIn(0f, 1f)
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    listState: LazyListState,
) {
    val colors = NavoTheme.colors
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + NavoSpacing.Large,
        ),
    ) {
        item(key = HEADER_ITEM_KEY) { SettingsHeader() }
        item(key = "appearance") {
            SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))
            ThemeModeSelector(
                options = state.themeOptions,
                selected = state.themeMode,
                onSelect = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
            )
        }
        item(key = "library") {
            SettingsSectionHeader(title = stringResource(R.string.settings_section_library))
            SettingsGroup {
                ShortTracksSetting(state = state, onIntent = onIntent)
                SettingsGroupDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_hidden_folders_title),
                    subtitle = if (state.hiddenFolderCount == 0) {
                        stringResource(R.string.settings_hidden_folders_none)
                    } else {
                        pluralStringResource(R.plurals.settings_hidden_folders_count, state.hiddenFolderCount, state.hiddenFolderCount)
                    },
                    onClick = { onIntent(SettingsIntent.HiddenFoldersClicked) },
                    leading = {
                        SettingsIconTile(icon = NavoIcons.Folder, containerColor = colors.accent, contentColor = colors.onAccent)
                    },
                )
                SettingsGroupDivider()
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_show_cover_art_title),
                    subtitle = stringResource(R.string.settings_show_cover_art_subtitle),
                    checked = state.showCoverArt,
                    onCheckedChange = { onIntent(SettingsIntent.ShowCoverArtToggled(it)) },
                    leading = { SettingsIconTile(icon = NavoIcons.Artwork, containerColor = colors.turquoise) },
                )
            }
        }
        item(key = "playback") {
            SettingsSectionHeader(title = stringResource(R.string.settings_section_playback))
            SettingsGroup {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_pause_on_disconnect_title),
                    subtitle = stringResource(R.string.settings_pause_on_disconnect_subtitle),
                    checked = state.pauseOnHeadphonesDisconnect,
                    onCheckedChange = { onIntent(SettingsIntent.PauseOnHeadphonesDisconnectToggled(it)) },
                    leading = { SettingsIconTile(icon = NavoIcons.Headphones, containerColor = colors.crimson) },
                )
                SettingsGroupDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_equalizer_title),
                    subtitle = state.equalizerSummary.label(),
                    onClick = { onIntent(SettingsIntent.EqualizerClicked) },
                    leading = {
                        SettingsIconTile(icon = NavoIcons.Equalizer, containerColor = colors.content, contentColor = colors.background)
                    },
                )
            }
        }
        item(key = "about") {
            SettingsSectionHeader(title = stringResource(R.string.settings_section_about))
            SettingsGroup {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_licenses_title),
                    onClick = { onIntent(SettingsIntent.LicensesClicked) },
                    leading = {
                        SettingsIconTile(icon = NavoIcons.Document, containerColor = colors.high, contentColor = colors.accent)
                    },
                )
            }
        }
        item(key = "footer") { SettingsFooter(versionName = state.versionName) }
    }
}

@Composable
private fun SettingsCompactBar(
    title: String,
    titleAlpha: () -> Float,
    titleIsHeading: Boolean,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = NavoSpacing.ScreenHorizontal, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavoIconButton(
                icon = NavoIcons.ChevronLeft,
                contentDescription = stringResource(R.string.settings_back),
                onClick = onBack,
                size = BackButtonSize,
            )
            Text(
                text = title,
                style = NavoTheme.typography.titleS,
                color = NavoTheme.colors.content,
                modifier = Modifier
                    .graphicsLayer { alpha = titleAlpha() }
                    .then(
                        if (titleIsHeading) Modifier.semantics { heading() } else Modifier.clearAndSetSemantics {},
                    ),
            )
        }
        Spacer(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(1.dp)
                .graphicsLayer { alpha = titleAlpha() }
                .background(NavoTheme.colors.divider),
        )
    }
}

@Composable
private fun SettingsHeader() {
    val colors = NavoTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .khotamWatermark(
                color = colors.ornament.copy(alpha = WATERMARK_ALPHA),
                diameter = 160.dp,
                endOffset = 48.dp,
            )
            .padding(start = NavoSpacing.ScreenHorizontal, end = NavoSpacing.ScreenHorizontal, top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = NavoTheme.typography.displayM,
            color = colors.content,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.settings_tagline),
            style = NavoTheme.typography.secondary,
            color = colors.contentSecondary,
        )
    }
}

@Composable
private fun ShortTracksSetting(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    val colors = NavoTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsInfoRow(
            title = stringResource(R.string.settings_min_duration_title),
            subtitle = stringResource(R.string.settings_min_duration_subtitle),
            leading = { SettingsIconTile(icon = NavoIcons.Timer, containerColor = colors.turquoise) },
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            contentPadding = PaddingValues(start = SettingsGroupDividerInset, end = NavoSpacing.Medium, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small),
        ) {
            items(state.durationOptions, key = { it.name }) { option ->
                NavoChip(
                    label = option.label(),
                    selected = option == state.minTrackDuration,
                    onClick = { onIntent(SettingsIntent.MinTrackDurationSelected(option)) },
                    role = Role.RadioButton,
                    unselectedContainerColor = colors.high,
                )
            }
        }
    }
}

@Composable
private fun SettingsFooter(versionName: String) {
    val colors = NavoTheme.colors
    val wordmark = stringResource(DesignSystemR.string.designsystem_wordmark)
    val version = stringResource(R.string.settings_version, versionName)
    val tagline = stringResource(R.string.settings_tagline)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = NavoSpacing.ExtraLarge, start = NavoSpacing.ScreenHorizontal, end = NavoSpacing.ScreenHorizontal)
            .clearAndSetSemantics { contentDescription = "$wordmark. $version. $tagline" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OrnamentDivider(modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Medallion(palette = MedallionPalettes.all[0], modifier = Modifier.size(FooterMedallionSize))
        Wordmark(small = true)
        Text(
            text = version,
            style = NavoTheme.typography.captionNumeric,
            color = colors.contentSecondary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = tagline,
            style = NavoTheme.typography.caption,
            color = colors.contentSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun SettingsTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = NavoSpacing.ScreenHorizontal, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        NavoIconButton(
            icon = NavoIcons.ChevronLeft,
            contentDescription = stringResource(R.string.settings_back),
            onClick = onBack,
        )
        Text(
            text = title,
            style = NavoTheme.typography.displayM,
            color = NavoTheme.colors.content,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun MinTrackDuration.label(): String =
    if (this == MinTrackDuration.Off) {
        stringResource(R.string.settings_min_duration_off)
    } else {
        stringResource(R.string.settings_min_duration_seconds, seconds)
    }

@Composable
private fun EqualizerSummary.label(): String = when (this) {
    EqualizerSummary.Loading -> stringResource(R.string.settings_equalizer_loading)
    EqualizerSummary.Off -> stringResource(R.string.settings_equalizer_off)
    EqualizerSummary.Custom -> stringResource(R.string.settings_equalizer_custom)
    EqualizerSummary.Unsupported -> stringResource(R.string.settings_equalizer_unsupported)
    is EqualizerSummary.Preset -> name
}

private val PreviewState = SettingsState(
    isLoading = false,
    minTrackDuration = MinTrackDuration.ThirtySeconds,
    hiddenFolderCount = 2,
    versionName = "1.0",
    equalizerSummary = EqualizerSummary.Preset("Rock"),
    themeMode = ThemeMode.Dark,
)

@Preview(name = "Dark", widthDp = 390, heightDp = 1400)
@Composable
private fun SettingsDarkPreview() {
    NavoTheme {
        SettingsScreen(state = PreviewState, onIntent = {})
    }
}

@Preview(name = "Light", widthDp = 390, heightDp = 1400)
@Composable
private fun SettingsLightPreview() {
    NavoTheme(darkTheme = false) {
        SettingsScreen(state = PreviewState.copy(themeMode = ThemeMode.Light), onIntent = {})
    }
}

@Preview(name = "Loading", widthDp = 390, heightDp = 600)
@Composable
private fun SettingsLoadingPreview() {
    NavoTheme {
        SettingsScreen(state = SettingsState(), onIntent = {})
    }
}

@Preview(name = "Error", widthDp = 390, heightDp = 600)
@Composable
private fun SettingsErrorPreview() {
    NavoTheme(darkTheme = false) {
        SettingsScreen(state = SettingsState(isLoading = false, loadFailed = true), onIntent = {})
    }
}
