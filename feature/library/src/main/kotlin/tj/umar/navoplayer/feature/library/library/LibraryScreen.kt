package tj.umar.navoplayer.feature.library.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.library.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    state: LibraryState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.library_title)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PrimaryTabRow(selectedTabIndex = state.tabs.indexOf(state.selectedTab)) {
                state.tabs.forEach { tab ->
                    Tab(
                        selected = tab == state.selectedTab,
                        onClick = { onIntent(LibraryIntent.TabSelected(tab)) },
                        text = { Text(stringResource(tab.titleRes)) },
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.library_placeholder_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LibraryScreenPreview() {
    NavoTheme {
        LibraryScreen(state = LibraryState(), onIntent = {})
    }
}

@PreviewLightDark
@Composable
private fun LibraryScreenAlbumsPreview() {
    NavoTheme {
        LibraryScreen(state = LibraryState(selectedTab = LibraryTab.Albums), onIntent = {})
    }
}
