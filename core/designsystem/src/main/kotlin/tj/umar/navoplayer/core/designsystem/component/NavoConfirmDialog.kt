package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

@Composable
fun NavoConfirmDialog(
    title: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
) {
    NavoDialog(
        title = title,
        confirmLabel = confirmLabel,
        dismissLabel = dismissLabel,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        content = text?.let { message ->
            {
                Text(
                    text = message,
                    style = NavoTheme.typography.body,
                    color = NavoTheme.colors.contentSecondary,
                )
            }
        },
    )
}

@Composable
fun NavoDialog(
    title: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    val colors = NavoTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.raised,
        titleContentColor = colors.content,
        textContentColor = colors.contentSecondary,
        title = { Text(text = title, style = NavoTheme.typography.titleS) },
        text = content,
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled) {
                Text(
                    text = confirmLabel,
                    style = NavoTheme.typography.labelStrong,
                    color = if (confirmEnabled) colors.accent else colors.contentMuted,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissLabel, style = NavoTheme.typography.label, color = colors.contentSecondary)
            }
        },
    )
}

@Preview
@Composable
private fun NavoConfirmDialogPreview() {
    NavoTheme {
        NavoConfirmDialog(
            title = "Удалить плейлист?",
            text = "Плейлист «Утро» будет удалён. Треки останутся на телефоне.",
            confirmLabel = "Удалить",
            dismissLabel = "Отмена",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
