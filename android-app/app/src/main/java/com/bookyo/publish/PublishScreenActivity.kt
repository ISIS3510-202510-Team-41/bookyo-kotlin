package com.bookyo.publish

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookyo.components.BookyoButton
import com.bookyo.components.BookyoTextField
import com.bookyo.components.rememberToastState
import com.bookyo.components.ToastHandler
import com.bookyo.R
import com.bookyo.components.ImageUploadBox
import com.bookyo.ui.BookyoTheme

class PublishScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookyoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PublishScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen() {
    val scrollState = rememberScrollState()
    val toastState = rememberToastState()

    // Form state
    var isbn by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Publish",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { /* Shopping cart action */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shopping_cart),
                            contentDescription = "Shopping Cart",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Image upload area with X pattern
            ImageUploadBox(
                onClick = {
                    // Handle image selection
                    toastState.showInfo("Image upload not implemented yet")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Form fields
            BookyoTextField(
                value = isbn,
                onValueChange = { isbn = it },
                label = "ISBN",
                modifier = Modifier.fillMaxWidth()
            )

            BookyoTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                modifier = Modifier.fillMaxWidth()
            )

            BookyoTextField(
                value = author,
                onValueChange = { author = it },
                label = "Author",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Publish button
            BookyoButton(
                text = "Publish",
                onClick = {
                    // Handle publish action
                    toastState.showSuccess("Book published successfully!")
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
            )
        }

        // Toast messages handler
        ToastHandler(toastState)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPublishScreen() {
    BookyoTheme {
        PublishScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPublishScreenDark() {
    BookyoTheme {
        PublishScreen()
    }
}