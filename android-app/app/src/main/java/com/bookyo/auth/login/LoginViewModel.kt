package com.bookyo.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookyo.auth.AmplifyAuthManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authManager: AmplifyAuthManager = AmplifyAuthManager()
): ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private fun validateLoginInput(): String? {
        return when {
            email.isBlank() -> "Email is required"
            !email.contains("@") -> "Invalid email format"
            password.isBlank() -> "Password is required"
            else -> null
        }

    }

    fun login(onSuccess: () -> Unit) {

        errorMessage = validateLoginInput()
        if (errorMessage!!.isEmpty()) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            authManager.signIn(email, password).onSuccess {
                isLoading = false
                onSuccess
            }.onFailure {
                isLoading = false
                errorMessage = it.localizedMessage ?: "Login Failed"
            }
        }
    }
}