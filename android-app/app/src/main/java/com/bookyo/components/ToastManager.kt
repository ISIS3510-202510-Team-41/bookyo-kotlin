package com.bookyo.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Class to manage toast state and messages
 */
class ToastState {
    var message: String = ""
    var type: ToastType = ToastType.INFO
    var visible: MutableState<Boolean> = mutableStateOf(false)

    fun show(msg: String, toastType: ToastType = ToastType.INFO) {
        message = msg
        type = toastType
        visible.value = true
    }

    fun showError(msg: String) = show(msg, ToastType.ERROR)
    fun showSuccess(msg: String) = show(msg, ToastType.SUCCESS)
    fun showInfo(msg: String) = show(msg, ToastType.INFO)

    fun hide() {
        visible.value = false
    }
}

/**
 * Create and remember a ToastState instance to be used across your UI
 */
@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

/**
 * A composable to render the toast and handle its state
 */
@Composable
fun ToastHandler(toastState: ToastState) {
    BookyoToast(
        message = toastState.message,
        type = toastState.type,
        visible = toastState.visible.value,
        onDismiss = { toastState.hide() }
    )
}