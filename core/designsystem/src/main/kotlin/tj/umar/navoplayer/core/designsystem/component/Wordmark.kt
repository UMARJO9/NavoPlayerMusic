package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import tj.umar.navoplayer.core.designsystem.R
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

@Composable
fun Wordmark(
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val typography = NavoTheme.typography
    Text(
        text = stringResource(R.string.designsystem_wordmark),
        style = if (small) typography.wordmarkSmall else typography.wordmark,
        color = NavoTheme.colors.content,
        modifier = modifier.semantics { heading() },
    )
}
