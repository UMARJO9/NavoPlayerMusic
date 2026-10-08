package tj.umar.navoplayer.feature.library.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.library.R

@Composable
internal fun ListSummary(
    title: String,
    subtitle: String,
    onSortClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = SummaryHorizontalPadding, top = 12.dp, end = SummaryHorizontalPadding, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.titleS, color = colors.content)
            Text(text = subtitle, style = typography.secondary, color = colors.contentSecondary)
        }
        NavoIconButton(
            icon = NavoIcons.Sort,
            contentDescription = stringResource(R.string.library_sort),
            onClick = onSortClick,
            containerColor = colors.raised,
            iconSize = 22.dp,
        )
        NavoButton(
            text = stringResource(R.string.library_shuffle),
            onClick = onShuffleClick,
            leadingIcon = NavoIcons.Shuffle,
            height = NavoSpacing.MinTouchTarget,
            contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
            textStyle = typography.label.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
