package com.bookyo.auth.signup

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookyo.BookyoApp
import com.bookyo.auth.AuthBaseActivity
import com.bookyo.components.BookyoButton
import com.bookyo.components.BookyoTextField
import com.bookyo.home.HomeScreenActivity
import com.bookyo.ui.BookyoTheme
import com.bookyo.R
import com.bookyo.components.BookyoLogo

class SignUpActivity: AuthBaseActivity() {
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun showAuthFlow() {
        setContent {
            BookyoTheme {
                SignUpFlow(
                    viewModel = viewModel,
                    onSignUpSuccess = { onSignupSuccess() }
                )
            }
        }
    }

    private fun onSignupSuccess() {
        // Start notification service after successful signup
        (applicationContext as? BookyoApp)?.startNotificationService()

        // Navigate to home screen
        val intent = Intent(this, HomeScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }



    @Composable
    private fun SignUpFlow(
        viewModel: SignUpViewModel,
        onSignUpSuccess: () -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            when (viewModel.currentState) {
                SignUpState.WaitingForConfirmation -> ConfirmationForm(viewModel, onSignUpSuccess)
                else -> SignUpForm(viewModel)
            }
        }
    }

    @Composable
    fun SignUpForm(viewModel: SignUpViewModel) {
        val scrollState = rememberScrollState()
        val showPassword = remember { mutableStateOf(false) }
        val showConfirmPassword = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            BookyoLogo(Modifier.size(250.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome Text
            Text(
                text = "Welcome to the marketplace!",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // First Name Field
            BookyoTextField(
                value = viewModel.firstName,
                onValueChange = { viewModel.firstName = it },
                label = "First Name",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Last Name Field
            BookyoTextField(
                value = viewModel.lastName,
                onValueChange = { viewModel.lastName = it },
                label = "Last Name",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            BookyoTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = "Email",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            BookyoTextField(
                value = viewModel.phone,
                onValueChange = { viewModel.phone = it },
                label = "Phone",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Address Field
            BookyoTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = "Address",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
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
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { showPassword.value = !showPassword.value }) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            BookyoTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                label = "Confirm Password",
                isPassword = showPassword.value,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword.value = !showConfirmPassword.value }) {
                        Icon(
                            painter = painterResource(
                                if (showConfirmPassword.value) R.drawable.visibility_24px
                                else R.drawable.visibility_24px
                            ),
                            contentDescription = "Toggle password visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            // Error message
            if (viewModel.currentState is SignUpState.Error) {
                Text(
                    text = (viewModel.currentState as SignUpState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            BookyoButton(
                text = if (viewModel.currentState is SignUpState.Loading) "Creating Account..." else "Sign up",
                onClick = { viewModel.signUp() },
                enabled = viewModel.currentState !is SignUpState.Loading,
                modifier = Modifier.width(124.dp)
            )
        }
    }

    @Composable
    fun ConfirmationForm(viewModel: SignUpViewModel, onConfirmationSuccess: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Confirm Your Email",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "We've sent a confirmation code to ${viewModel.email}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            BookyoTextField(
                value = viewModel.confirmationCode,
                onValueChange = { viewModel.confirmationCode = it },
                label = "Confirmation Code",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )

            // Error message for confirmation
            if (viewModel.currentState is SignUpState.Error) {
                Text(
                    text = (viewModel.currentState as SignUpState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BookyoButton(
                text = if (viewModel.currentState is SignUpState.Loading) "Confirming..." else "Confirm Email",
                onClick = { viewModel.confirmSignUp(onConfirmationSuccess) },
                enabled = viewModel.currentState !is SignUpState.Loading
            )
        }
    }
}