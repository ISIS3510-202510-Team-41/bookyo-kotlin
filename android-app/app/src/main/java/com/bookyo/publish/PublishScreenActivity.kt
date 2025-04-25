package com.bookyo.publish

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.bookyo.R
import com.bookyo.components.BookyoButton
import com.bookyo.components.BookyoTextField
import com.bookyo.components.BottomNavigationBar
import com.bookyo.components.ToastHandler
import com.bookyo.components.rememberToastState
import com.bookyo.home.HomeScreenActivity
import com.bookyo.ui.BookyoTheme
import com.bookyo.ui.lightGray
import com.bookyo.ui.whiteGray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val toastState = rememberToastState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedItem by remember { mutableIntStateOf(2) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Create a temporary file for camera image
    val getTempFileUri = remember {
        {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoFile = File(context.cacheDir, "camera/image_$timeStamp.jpg").apply {
                parentFile?.mkdirs()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
        }
    }

    // Remember the current photo URI
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleImageSelected(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            photoUri?.let { viewModel.handleImageSelected(it, isFromCamera = true) }
        } else {
            toastState.showError("Failed to capture image")
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, launch camera
            photoUri = getTempFileUri()
            photoUri?.let { uri ->
                cameraLauncher.launch(uri)
            } ?: run {
                toastState.showError("Failed to create image file")
            }
        } else {
            // Permission denied
            toastState.showError("Camera permission is required to take photos")
        }
    }

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

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Image Source") },
            text = { Text("Select an image from gallery or take a photo with camera") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    // Check and request camera permission if needed
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                            // Permission already granted, launch camera
                            photoUri = getTempFileUri()
                            photoUri?.let { uri ->
                                cameraLauncher.launch(uri)
                            } ?: run {
                                toastState.showError("Failed to create image file")
                            }
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }) {
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Gallery")
                }
            }
        )
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
                    IconButton(onClick = {
                        // Shopping cart action
                        toastState.showInfo("Shopping cart not implemented yet")
                    }) {
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
            BottomNavigationBar(currentScreenIndex = selectedItem)
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
            // Image upload section
            if (viewModel.selectedImageUri != null) {
                // Display selected image with change option
                BookyoImagePreview(
                    imageUri = viewModel.selectedImageUri,
                    onChangeImage = { showImageSourceDialog = true }
                )
            } else {
                // Empty image upload box
                EmptyImageUploadBox(
                    onClick = { showImageSourceDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            BookyoTextField(
                value = viewModel.isbn,
                onValueChange = { viewModel.isbn = it },
                label = "ISBN",
                modifier = Modifier.fillMaxWidth()
            )

            BookyoTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = "Title",
                modifier = Modifier.fillMaxWidth()
            )

            BookyoTextField(
                value = viewModel.authorName,
                onValueChange = { viewModel.authorName = it },
                label = "Author",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            BookyoButton(
                text = "Publish",
                onClick = {
                    viewModel.publishBook()
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f),
                isPrimary = false
            )
        }

        ToastHandler(toastState)
    }
}

@Composable
fun EmptyImageUploadBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = lightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(whiteGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Draw the X pattern for empty state
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.dp.toPx()
            val lineColor = lightGray.copy(alpha = 0.5f)

            // Draw diagonal lines to form an X
            drawLine(
                color = lineColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            drawLine(
                color = lineColor,
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // Overlay text
        Text(
            text = "Upload Book Cover Image",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BookyoImagePreview(
    imageUri: Uri?,
    onChangeImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Display the image with Coil
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Book cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Change image button
        BookyoButton(
            text = "Change Image",
            onClick = onChangeImage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .width(150.dp)
        )
    }
}