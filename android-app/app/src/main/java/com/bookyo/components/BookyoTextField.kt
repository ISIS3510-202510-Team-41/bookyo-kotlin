package com.bookyo.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * A reusable text field component styled according to the Bookyo design system.
 *
 * @param value The current text value to be shown in the text field
 * @param onValueChange Callback that is triggered when the text field's value changes
 * @param label The label/placeholder text to display
 * @param modifier Modifier to be applied to the text field
 * @param isPassword Whether the text field should hide the input as a password
 * @param keyboardOptions Options controlling keyboard behavior
 * @param keyboardActions Actions to perform based on keyboard input
 * @param isError Whether the text field should indicate an error state
 * @param errorMessage Message to display when in error state
 */
@Composable
fun BookyoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
            errorCursorColor = MaterialTheme.colorScheme.onErrorContainer,
            errorIndicatorColor = Color.Transparent,
            errorTextColor = MaterialTheme.colorScheme.onErrorContainer,
            errorPlaceholderColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        supportingText = if (isError && !errorMessage.isNullOrEmpty()) {
            { Text(text = errorMessage) }
        } else null,
        singleLine = true,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}