package com.bookyo.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

/**
 * A reusable toast component for displaying notifications.
 *
 * @param message The message to display in the toast
 * @param type The type of toast (SUCCESS, ERROR, INFO, WARNING)
 * @param visible Whether the toast is currently visible
 * @param onDismiss Callback triggered when the toast is dismissed
 * @param durationMillis Duration in milliseconds for how long the toast should be displayed
 */
@Composable
fun BookyoToast(
    message: String,
    type: ToastType = ToastType.INFO,
    visible: Boolean,
    onDismiss: () -> Unit,
    durationMillis: Long = 3000
) {
    var isVisible by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        isVisible = visible
        if (visible) {
            delay(durationMillis)
            isVisible = false
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(animationSpec = tween(300)) { -it },
            exit = fadeOut(animationSpec = tween(300)) +
                    slideOutVertically(animationSpec = tween(300)) { -it }
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(getBackgroundColor(type))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = getTextColor(type),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun getBackgroundColor(type: ToastType): Color {
    return when (type) {
        ToastType.SUCCESS -> MaterialTheme.colorScheme.primary
        ToastType.ERROR -> MaterialTheme.colorScheme.error
        ToastType.WARNING -> MaterialTheme.colorScheme.tertiary
        ToastType.INFO -> MaterialTheme.colorScheme.secondary
    }
}

@Composable
private fun getTextColor(type: ToastType): Color {
    return when (type) {
        ToastType.SUCCESS -> MaterialTheme.colorScheme.onPrimary
        ToastType.ERROR -> MaterialTheme.colorScheme.onError
        ToastType.WARNING -> MaterialTheme.colorScheme.onTertiary
        ToastType.INFO -> MaterialTheme.colorScheme.onSecondary
    }
}

