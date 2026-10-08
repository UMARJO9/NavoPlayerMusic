package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalette
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val PHASE_ENTER_MILLIS = 260
private const val PHASE_EXIT_MILLIS = 160

enum class ContentPhase { Loading, Content, Empty, Error }

fun contentPhase(hasContent: Boolean, isLoading: Boolean, loadFailed: Boolean): ContentPhase = when {
    hasContent -> ContentPhase.Content
    isLoading -> ContentPhase.Loading
    loadFailed -> ContentPhase.Error
    else -> ContentPhase.Empty
}

@Composable
fun PhasedContent(
    phase: ContentPhase,
    modifier: Modifier = Modifier,
    empty: @Composable () -> Unit,
    error: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = phase,
        transitionSpec = { fadeIn(tween(PHASE_ENTER_MILLIS)) togetherWith fadeOut(tween(PHASE_EXIT_MILLIS)) },
        modifier = modifier,
        label = "contentPhase",
    ) { target ->
        when (target) {
            ContentPhase.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavoTheme.colors.accent)
            }
            ContentPhase.Content -> content()
            ContentPhase.Empty -> empty()
            ContentPhase.Error -> error()
        }
    }
}

@Composable
fun StateMessage(
    message: String,
    palette: MedallionPalette,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = NavoSpacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Medallion(palette = palette, modifier = Modifier.size(104.dp))
            Text(
                text = message,
                style = NavoTheme.typography.titleS,
                color = NavoTheme.colors.content,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null) {
                NavoButton(text = actionLabel, onClick = onAction, height = NavoSpacing.MinTouchTarget)
            }
        }
    }
}
