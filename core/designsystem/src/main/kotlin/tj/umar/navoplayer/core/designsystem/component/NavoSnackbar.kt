package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

@Composable
fun NavoSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        NavoSnackbar(data = data)
    }
}

@Composable
private fun NavoSnackbar(data: SnackbarData) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    val actionLabel = data.visuals.actionLabel
    Snackbar(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        shape = NavoShapes.Snackbar,
        containerColor = colors.high,
        contentColor = colors.contentOnHigh,
        action = actionLabel?.let { label ->
            {
                TextButton(onClick = data::performAction) {
                    Text(text = label, style = typography.labelStrong, color = colors.accent)
                }
            }
        },
    ) {
        Text(text = data.visuals.message, style = typography.secondary, color = colors.contentOnHigh)
    }
}

private class PreviewSnackbarData(message: String, actionLabel: String?) : SnackbarData {
    override val visuals: SnackbarVisuals = object : SnackbarVisuals {
        override val message: String = message
        override val actionLabel: String? = actionLabel
        override val withDismissAction: Boolean = false
        override val duration: SnackbarDuration = SnackbarDuration.Long
    }

    override fun performAction() = Unit

    override fun dismiss() = Unit
}

@Preview(widthDp = 390)
@Composable
private fun NavoSnackbarPreview() {
    NavoTheme {
        Box(modifier = Modifier.background(NavoTheme.colors.background).padding(8.dp)) {
            NavoSnackbar(data = PreviewSnackbarData(message = "“Ҷавонӣ” removed", actionLabel = "Undo"))
        }
    }
}
