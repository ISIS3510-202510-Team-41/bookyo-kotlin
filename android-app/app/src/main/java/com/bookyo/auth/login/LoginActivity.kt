package com.bookyo.auth.login

import android.content.Intent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.bookyo.components.BookyoButton
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
                    viewModel = viewModel, onLoginSuccess = {
                        val intent = Intent(this, HomeScreenActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    fun LoginScreen(
        viewModel: LoginViewModel, onLoginSuccess: () -> Unit
    ) {
        val showPassword = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(
                modifier = Modifier.fillMaxWidth().padding(32.dp)
            )
            Form(
                viewModel, showPassword, onLoginSuccess, modifier = Modifier.padding(0.dp, 40.dp)
            )
        }
    }
}

@Composable
private fun Form(
    viewModel: LoginViewModel,
    showPassword: MutableState<Boolean>,
    onLoginSuccess: () -> Unit,
    modifier: Modifier
) {

    val toastState = rememberToastState()

    BookyoTextField(
        value = viewModel.email,
        onValueChange = { viewModel.email = it },
        label = "Email",
        modifier = Modifier.padding(52.dp, 0.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    BookyoTextField(
        value = viewModel.password,
        onValueChange = {viewModel.password = it},
        label = "Password",
        isPassword = true,
        modifier = Modifier.padding(52.dp, 0.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
        ),
        trailingIcon = {
            IconButton(onClick = { showPassword.value = !showPassword.value }) {
                Icon(
                    painter = painterResource(R.drawable.visibility_24px),
                    contentDescription = "Toggle password visibility",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )

    viewModel.errorMessage?.let {error ->
        toastState.showError(error)
        ToastHandler(toastState)
    }

    Spacer(modifier = Modifier.height(16.dp))

    BookyoButton(
        onClick = { viewModel.login(onLoginSuccess) },
        enabled = !viewModel.isLoading,
        text = if (viewModel.isLoading) "Loading..." else "Login"
    )
}

@Composable
private fun Header(modifier: Modifier) {
    Box(
        modifier = modifier, contentAlignment = Alignment.CenterStart
    )  {
        Text(
            text = "Log in!",
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}