package com.bookyo.listing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.bookyo.components.BookThumbnail
import com.bookyo.components.BookyoButton
import com.bookyo.components.BookyoTextField
import com.bookyo.components.ToastHandler
import com.bookyo.components.rememberToastState
import com.bookyo.ui.BookyoTheme
import com.bookyo.ui.blue
import com.bookyo.ui.lightGray
import com.bookyo.ui.whiteGray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateListingActivity : ComponentActivity() {

    private val viewModel: CreateListingViewModel by viewModels {
        CreateListingViewModelFactory(application)
    }

    companion object {
        private const val EXTRA_BOOK_ID = "extra_book_id"

        fun createIntent(context: Context, bookId: String): Intent {
            return Intent(context, CreateListingActivity::class.java).apply {
                putExtra(EXTRA_BOOK_ID, bookId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the book ID from the intent
        val bookId = intent.getStringExtra(EXTRA_BOOK_ID)

        if (bookId == null) {
            finish()
            return
        }

        // Load the book details
        viewModel.initialize(bookId)

        setContent {
            BookyoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateListingScreen(
                        viewModel = viewModel,
                        onNavigateBack = { finish() },
                        onListingCreated = {
                            // Navigate back to book detail on success
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    viewModel: CreateListingViewModel,
    onNavigateBack: () -> Unit,
    onListingCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val toastState = rememberToastState()
    val context = LocalContext.current

    // Success navigation
    LaunchedEffect(uiState.listingCreated) {
        if (uiState.listingCreated) {
            onListingCreated()
        }
    }

    // Error and success messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            toastState.showError(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            toastState.showSuccess(it)
            viewModel.clearMessages()
        }
    }

    // Image selection dialog
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
        uri?.let { viewModel.addListingImage(it) }
    }

    // Camera picker
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            photoUri?.let { viewModel.addListingImage(it, isFromCamera = true) }
        } else {
            toastState.showError("Failed to capture image")
        }
    }

    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Image Source") },
            text = { Text("Select an image from gallery or take a photo with camera") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    photoUri = getTempFileUri()
                    photoUri?.let { uri ->
                        cameraLauncher.launch(uri)
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
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Create Listing",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Connectivity status banner
                ConnectivityStatusBanner(isConnected = uiState.isConnected)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = blue
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Book info card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Book thumbnail
                            BookThumbnail(
                                thumbnailKey = uiState.bookThumbnail,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Book info
                            Column {
                                Text(
                                    text = uiState.bookTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "by ${uiState.authorName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "ISBN: ${uiState.bookIsbn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price input
                    BookyoTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.updatePrice(it) },
                        label = "Price ($)",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = uiState.priceError != null,
                        errorMessage = uiState.priceError
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Photo section
                    Text(
                        text = "Photos (${uiState.images.size}/5)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Images grid or empty state
                    if (uiState.images.isEmpty()) {
                        // Empty image state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = lightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(whiteGray)
                                .clickable(
                                    enabled = uiState.images.size < 5,
                                    onClick = { showImageSourceDialog = true }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap to add photos of your book",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Show image grid
                        ImageGrid(
                            images = uiState.images,
                            onAddImage = {
                                if (uiState.images.size < 5) {
                                    showImageSourceDialog = true
                                } else {
                                    toastState.showInfo("Maximum 5 images allowed")
                                }
                            },
                            onRemoveImage = { index ->
                                viewModel.removeImage(index)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Create Listing Button
                    BookyoButton(
                        text = if (uiState.isSubmitting) "Creating..." else "Create Listing",
                        onClick = { viewModel.createListing() },
                        enabled = !uiState.isSubmitting,
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            ToastHandler(toastState = toastState)
        }
    }
}

@Composable
fun ImageGrid(
    images: List<Uri>,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Show existing images
        items(images) { uri ->
            val index = images.indexOf(uri)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = lightGray,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                // Display the image
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Listing image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Remove button
                IconButton(
                    onClick = { onRemoveImage(index) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .padding(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Add button if we have less than 5 images
        item {
            if (images.size < 5) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = lightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(whiteGray)
                        .clickable(onClick = onAddImage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add image",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectivityStatusBanner(isConnected: Boolean) {
    AnimatedVisibility(
        visible = !isConnected,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "You're offline. Listing will be created when connectivity is restored.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}