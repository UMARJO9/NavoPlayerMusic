package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

@Composable
fun NavoTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
    focusRequester: FocusRequester = remember { FocusRequester() },
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    val colors = NavoTheme.colors
    val textStyle = NavoTheme.typography.itemTitle.copy(color = colors.content)
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(NavoShapes.Pill)
            .background(colors.high)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.text.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle,
                color = colors.contentOnHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.limitedTo(maxLength)) },
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
    }
}

private fun TextFieldValue.limitedTo(maxLength: Int): TextFieldValue {
    if (text.length <= maxLength) return this
    val limited = text.take(maxLength)
    return copy(text = limited, selection = TextRange(selection.end.coerceAtMost(limited.length)))
}

@Preview
@Composable
private fun NavoTextFieldPreview() {
    NavoTheme {
        Box(modifier = Modifier.background(NavoTheme.colors.raised).padding(16.dp)) {
            NavoTextField(
                value = TextFieldValue("Утро в горах"),
                onValueChange = {},
                placeholder = "Название",
            )
        }
    }
}
