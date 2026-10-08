package tj.umar.navoplayer.core.ui.playlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import tj.umar.navoplayer.core.designsystem.component.NavoDialog
import tj.umar.navoplayer.core.designsystem.component.NavoTextField
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.playlist.PLAYLIST_NAME_MAX_LENGTH
import tj.umar.navoplayer.core.domain.playlist.normalizePlaylistName
import tj.umar.navoplayer.core.ui.R

@Composable
fun PlaylistNameDialog(
    title: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialName, selection = TextRange(0, initialName.length)))
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val isValid = normalizePlaylistName(name.text) != null
    val confirm = { if (isValid) onConfirm(name.text) }
    NavoDialog(
        title = title,
        confirmLabel = confirmLabel,
        dismissLabel = stringResource(R.string.core_ui_cancel),
        onConfirm = confirm,
        onDismiss = onDismiss,
        confirmEnabled = isValid,
        modifier = modifier,
    ) {
        NavoTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.core_ui_playlist_name),
            maxLength = PLAYLIST_NAME_MAX_LENGTH,
            focusRequester = focusRequester,
            onImeAction = confirm,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun PlaylistNameDialogPreview() {
    NavoTheme {
        PlaylistNameDialog(
            title = "Новый плейлист",
            confirmLabel = "Создать",
            initialName = "Утро",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
