package com.bookyo

import android.content.Intent

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment



import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookyo.auth.login.LoginActivity
import com.bookyo.auth.signup.SignUpActivity
import com.bookyo.components.BookyoButton
import com.bookyo.ui.BookyoTheme

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BookyoTheme {
                WelcomeScreen(
                    onLoginClick = { startActivity(Intent(this, LoginActivity::class.java)) },
                    onSignUpClick = { startActivity(Intent(this, SignUpActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
        MainContent(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState).padding(horizontal = 32.dp),
            onLoginClick,
            onSignUpClick
        )
    }

}

@Composable
fun MainContent(
    modifier: Modifier,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Log into the\nmarketplace...",

        )

        Spacer(modifier = Modifier.height(16.dp))

        BookyoButton(
            onClick = onLoginClick,
            text = "Login",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Or sign up now!",
        )

        Spacer(modifier = Modifier.height(8.dp))

        BookyoButton(
            onClick = onSignUpClick,
            text = "Sign up",
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Welcome to Bookyo!",
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    BookyoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WelcomeScreen(
                onLoginClick = {  },
                onSignUpClick = {  }
            )
        }
    }
}

@Preview(showBackground = true, name = "Welcome Screen Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenDarkPreview() {
    BookyoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WelcomeScreen(
                onLoginClick = {  },
                onSignUpClick = {  }
            )
        }
    }
}
