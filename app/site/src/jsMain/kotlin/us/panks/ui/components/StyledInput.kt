package us.panks.ui.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.silk.components.forms.TextInput
import us.panks.inputModifier

@Composable
fun StyledInput(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    password: Boolean = false,
) {
    TextInput(
        text = text,
        onTextChange = onTextChange,
        password = password,
        placeholder = placeholder.ifEmpty { null },
        modifier = inputModifier().then(modifier),
    )
}
