package com.bookyo.publish

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect

import com.bookyo.R
import com.bookyo.components.BottomNavigationBar
import com.bookyo.components.ImageUploadBox
import com.bookyo.home.HomeScreenActivity
import com.bookyo.ui.BookyoTheme

class PublishScreenActivity : ComponentActivity() {

    private val viewModel: PublishViewModel by viewModels {
        PublishViewModelFactory(application)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookyoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PublishScreen(viewModel = viewModel, onPublishSuccess = {
                        val intent = Intent(this, HomeScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(viewModel: PublishViewModel, onPublishSuccess: () -> Unit) {
    val scrollState = rememberScrollState()
    val toastState = rememberToastState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedItem by remember { mutableIntStateOf(2) }

    var isbn by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleImageSelected(it) }
    }

    // Handle UI state changes
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            toastState.showError(it)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            toastState.showSuccess(it)
            // Navigate on success message
            if (uiState.publishState == PublishState.SUCCESS) {
                onPublishSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Publish",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            // Shopping cart action
                            toastState.showInfo("Shopping cart not implemented yet") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shopping_cart),
                            contentDescription = "Shopping Cart",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(currentScreenIndex = currentScreenIndex)
        },
        containerColor = MaterialTheme.colorScheme.surface
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
            ImageUploadBox(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                    toastState.showInfo("Image upload not implemented yet")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            BookyoButton(
                text = "Publish",
                onClick = {
                    // Handle publish action
                    toastState.showSuccess("Book published successfully!")
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f),
                isPrimary = false
                )
        }

        ToastHandler(toastState)
    }
}
