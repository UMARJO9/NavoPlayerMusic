package tj.umar.navoplayer.core.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.NavoTextField
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.playlist.PLAYLIST_NAME_MAX_LENGTH
import tj.umar.navoplayer.core.domain.playlist.normalizePlaylistName
import tj.umar.navoplayer.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistNameSheet(
    title: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
        containerColor = NavoTheme.colors.raised,
        contentColor = NavoTheme.colors.content,
    ) {
        PlaylistNameForm(
            title = title,
            confirmLabel = confirmLabel,
            initialName = initialName,
            onConfirm = onConfirm,
            onCancel = onDismiss,
        )
    }
}

@Composable
fun PlaylistNameForm(
    title: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(start = NavoSpacing.ScreenHorizontal, end = NavoSpacing.ScreenHorizontal, bottom = NavoSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(NavoSpacing.Medium),
    ) {
        Text(
            text = title,
            style = NavoTheme.typography.titleS,
            color = NavoTheme.colors.content,
            modifier = Modifier.semantics { heading() },
        )
        NavoTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.core_ui_playlist_name),
            maxLength = PLAYLIST_NAME_MAX_LENGTH,
            focusRequester = focusRequester,
            onImeAction = confirm,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.core_ui_cancel),
                    style = NavoTheme.typography.label,
                    color = NavoTheme.colors.contentSecondary,
                )
            }
            NavoButton(
                text = confirmLabel,
                onClick = confirm,
                enabled = isValid,
                height = NavoSpacing.MinTouchTarget,
            )
        }
    }
}

@Preview
@Composable
private fun PlaylistNameFormPreview() {
    NavoTheme {
        PlaylistNameForm(
            title = "Новый плейлист",
            confirmLabel = "Создать",
            initialName = "Утро",
            onConfirm = {},
            onCancel = {},
            modifier = Modifier
                .background(NavoTheme.colors.raised)
                .padding(top = 16.dp),
        )
    }
}
