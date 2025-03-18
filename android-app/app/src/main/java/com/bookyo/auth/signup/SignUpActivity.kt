package com.bookyo.auth.signup


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookyo.auth.AuthBaseActivity
import com.bookyo.components.BookyoButton
import com.bookyo.components.BookyoTextField
import com.bookyo.ui.BookyoTheme
import com.example.bookyo.R

class SignUpActivity: AuthBaseActivity() {
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun showAuthFlow() {
        setContent {
            SignUpFlow(viewModel, {/*intent*/})
        }
    }


    @Composable
    private fun SignUpFlow(
        viewModel: SignUpViewModel, onSignUpSuccess: () -> Unit
    ) {
        BookyoTheme {
            SignUpScreen(viewModel = viewModel, onSignUpSuccess = onSignUpSuccess )
        }
    }


    @Composable
    fun SignUpScreen(
        viewModel: SignUpViewModel, onSignUpSuccess: () -> Unit
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Header(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                state = viewModel.currentState
            )

            when (viewModel.currentState) {
                SignUpState.WaitingForConfirmation -> ConfirmationForm(viewModel, onSignUpSuccess)
                else -> SignUpForm(viewModel, modifier = Modifier.verticalScroll(scrollState))
            }
        }
    }

    @Composable
    fun Header(modifier: Modifier, state: SignUpState) {
        Box(
            modifier = modifier, contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when (state) {
                    SignUpState.WaitingForConfirmation -> "Confirm Your Email"
                    else -> "Create Your Account"
                }, style = MaterialTheme.typography.displayMedium
            )
        }
    }

    @Composable
    fun SignUpForm(viewModel: SignUpViewModel, modifier: Modifier) {
        val showPassword = remember { mutableStateOf(false) }

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Existing form fields...
            BookyoTextField(
                value = viewModel.firstName,
                onValueChange = { viewModel.firstName = it },
                label = "First Name",
                modifier = Modifier.padding(52.dp, 0.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookyoTextField(
                value = viewModel.lastName,
                onValueChange = { viewModel.lastName = it },
                label = "Last Name",
                modifier = Modifier.padding(52.dp, 0.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookyoTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = "Email Address",
                modifier = Modifier.padding(52.dp, 0.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookyoTextField(
                value = viewModel.phone,
                onValueChange = { viewModel.phone = it },
                label = "Phone",
                modifier = Modifier.padding(52.dp, 0.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookyoTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = "Address",
                modifier = Modifier.padding(52.dp, 0.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            BookyoTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = "Password",
                modifier = Modifier.padding(52.dp, 0.dp),
                isPassword = true,
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
                })

            Spacer(modifier = Modifier.height(16.dp))

            BookyoTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                label = "Confirm Password",
                modifier = Modifier.padding(52.dp, 0.dp),
                isPassword = true,
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
                })

            // Error message
            when (viewModel.currentState) {
                is SignUpState.Error -> {
                    Text(
                        text = (viewModel.currentState as SignUpState.Error).message,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            BookyoButton(
                onClick = { viewModel.signUp() },
                enabled = viewModel.currentState !is SignUpState.Loading,
                text = if (viewModel.currentState is SignUpState.Loading) "Creating Account..." else "Sign Up"
            )
        }
    }

    @Composable
    fun ConfirmationForm(viewModel: SignUpViewModel, onConfirmationSuccess: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "We've sent a confirmation code to ${viewModel.email}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            BookyoTextField(
                value = viewModel.confirmationCode,
                onValueChange = { viewModel.confirmationCode = it },
                label = "Confirmation Code",
                modifier = Modifier.padding(52.dp, 0.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error message for confirmation
            when (viewModel.currentState) {
                is SignUpState.Error -> {
                    Text(
                        text = (viewModel.currentState as SignUpState.Error).message,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                else -> {}
            }

            BookyoButton(
                onClick = { viewModel.confirmSignUp(onConfirmationSuccess) },
                enabled = viewModel.currentState !is SignUpState.Loading,
                text = if (viewModel.currentState is SignUpState.Loading) "Confirming..." else "Confirm Email"
            )

            Spacer(modifier = Modifier.height(16.dp))

        }
    }


}