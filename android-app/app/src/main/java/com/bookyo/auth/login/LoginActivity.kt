package com.bookyo.auth.login

import android.content.Intent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bookyo.R
import com.bookyo.auth.AuthBaseActivity
import com.bookyo.auth.signup.SignUpActivity
import com.bookyo.components.BookyoButton
import com.bookyo.components.BookyoLogo
import com.bookyo.components.BookyoTextField
import com.bookyo.components.ToastHandler
import com.bookyo.components.rememberToastState
import com.bookyo.home.HomeScreenActivity
import com.bookyo.ui.BookyoTheme


class LoginActivity: AuthBaseActivity() {
    private val viewModel: LoginViewModel by viewModels()

    override fun showAuthFlow() {
        setContent {
            BookyoTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        val intent = Intent(this, HomeScreenActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onRegisterClick = {
                        val intent = Intent(this, SignUpActivity::class.java)
                        startActivity(intent)

                    }
                )
            }
        }
    }

    @Composable
    fun LoginScreen(
        viewModel: LoginViewModel,
        onLoginSuccess: () -> Unit,
        onRegisterClick: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
        ) {
            val showPassword = remember { mutableStateOf(false) }
            val toastState = rememberToastState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))


                BookyoLogo(modifier = Modifier.size(250.dp))

                Spacer(modifier = Modifier.height(50.dp))

                // Username/Email Field
                BookyoTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = "Username",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field

                BookyoTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = "Password",
                    isPassword = showPassword.value,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = {  }) {
                            Icon(
                                painter = painterResource(
                                    if (showPassword.value) R.drawable.visibility_24px
                                    else R.drawable.visibility_24px
                                ),
                                contentDescription = "Toggle password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                // Error message
                viewModel.errorMessage?.let { error ->
                    toastState.showError(error)
                    ToastHandler(toastState)
                }

                Spacer(modifier = Modifier.height(16.dp))


                // Login Button
                BookyoButton(
                    text = if (viewModel.isLoading) "Loading..." else "Login",
                    onClick = { viewModel.login(onLoginSuccess) },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.width(124.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Register Button
                BookyoButton(
                    text = "Register",
                    onClick = onRegisterClick,
                    modifier = Modifier.width(124.dp)
                )
            }
        }
    }
}