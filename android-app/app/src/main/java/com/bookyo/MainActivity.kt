package com.bookyo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview
import com.bookyo.components.BookyoTextField
import com.bookyo.ui.BookyoTheme
import com.bookyo.ui.components.BookyoButton

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BookyoTheme {
                WelcomeScreen()
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    Column {
        Text(text = "Hello Kotlin Title", style = MaterialTheme.typography.displayLarge)
        Text(text = "Hello Kotlin subtitle", style = MaterialTheme.typography.titleMedium)
        Text(text = "Hello Kotlin body", style = MaterialTheme.typography.bodySmall)
        BookyoTextField(
            label = "Text Field",
            value = "wordle",
            onValueChange = {} ,
            isPassword = false,
            isError = false,
            errorMessage = "Text must be filled"
        )
        BookyoButton(
            text = "Button",
            onClick = {},
            enabled = true
        )
        BookyoButton(
            text = "Button",
            onClick = {},
            enabled = true,
            isError = true
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
            WelcomeScreen()
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
            WelcomeScreen()
        }
    }
}
