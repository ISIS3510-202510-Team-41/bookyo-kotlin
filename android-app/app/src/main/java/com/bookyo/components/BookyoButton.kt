package com.bookyo.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A reusable button component styled according to the Bookyo design system.
 * The button has no outline and uses the app's primary color scheme.
 *
 * @param text The text to display on the button
 * @param onClick Callback that is triggered when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param isError Whether the button should be displayed in error/destructive style
 * @param isPrimary Whether the button should be displayed in primary style or secondary style
 */
@Composable
fun BookyoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    isPrimary: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        enabled = enabled,
        shape = RoundedCornerShape(40.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isError -> MaterialTheme.colorScheme.error
                isPrimary -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            },
            contentColor = when {
                isError -> MaterialTheme.colorScheme.onError
                isPrimary -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSecondary
            },
            disabledContainerColor = when {
                isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                isPrimary -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            },
            disabledContentColor = when {
                isError -> MaterialTheme.colorScheme.onError.copy(alpha = 0.5f)
                isPrimary -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}