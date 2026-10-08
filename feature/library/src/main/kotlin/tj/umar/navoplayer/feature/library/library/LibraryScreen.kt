package tj.umar.navoplayer.feature.library.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoChip
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.Wordmark
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.library.R

private const val TAB_ENTER_MILLIS = 220
private const val TAB_EXIT_MILLIS = 150

@Composable
internal fun LibraryScreen(
    state: LibraryState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        LibraryHeader(onIntent = onIntent)
        LibraryTabs(selectedTab = state.selectedTab, onIntent = onIntent)
        AnimatedContent(
            targetState = state.selectedTab,
            transitionSpec = { fadeIn(tween(TAB_ENTER_MILLIS)) togetherWith fadeOut(tween(TAB_EXIT_MILLIS)) },
            modifier = Modifier.fillMaxSize(),
            label = "libraryTab",
        ) { tab ->
            when (tab) {
                LibraryTab.Tracks -> TracksTabContent(
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier.fillMaxSize(),
                )
                LibraryTab.Playlists -> PlaylistsTabContent(state, onIntent, Modifier.fillMaxSize())
                LibraryTab.Albums -> AlbumsTabContent(state, onIntent, Modifier.fillMaxSize())
                LibraryTab.Artists -> ArtistsTabContent(state, onIntent, Modifier.fillMaxSize())
                LibraryTab.Folders -> FoldersTabContent(state, onIntent, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun LibraryHeader(onIntent: (LibraryIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = NavoSpacing.ScreenHorizontal, top = 8.dp, end = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Wordmark()
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(NavoSpacing.ExtraSmall)) {
            NavoIconButton(
                icon = NavoIcons.Search,
                contentDescription = stringResource(R.string.library_search),
                onClick = { onIntent(LibraryIntent.SearchClicked) },
            )
            NavoIconButton(
                icon = NavoIcons.Settings,
                contentDescription = stringResource(R.string.library_settings),
                onClick = { onIntent(LibraryIntent.SettingsClicked) },
            )
        }
    }
}

@Composable
private fun LibraryTabs(selectedTab: LibraryTab, onIntent: (LibraryIntent) -> Unit) {
    val sectionsDescription = stringResource(R.string.library_sections)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .semantics { contentDescription = sectionsDescription }
            .selectableGroup()
            .padding(horizontal = NavoSpacing.ScreenHorizontal, vertical = NavoSpacing.Small),
        horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small),
    ) {
        LibraryTab.entries.forEach { tab ->
            NavoChip(
                label = stringResource(tab.titleRes),
                selected = tab == selectedTab,
                onClick = { onIntent(LibraryIntent.TabSelected(tab)) },
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LibraryScreenPreview() {
    NavoTheme {
        LibraryScreen(
            state = LibraryState(isLoadingTracks = false, tracks = previewTracks),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LibraryScreenAlbumsPreview() {
    NavoTheme {
        LibraryScreen(state = previewLibraryState.copy(selectedTab = LibraryTab.Albums), onIntent = {})
    }
}
