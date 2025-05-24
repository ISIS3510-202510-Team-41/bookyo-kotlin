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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.bookyo.ui.orange
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

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedItem by remember { mutableIntStateOf(2) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Tab titles
    val tabTitles = listOf("Publish Book", "Pending (${uiState.pendingPublishes.size})")

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
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            toastState.showSuccess(it)
            viewModel.clearMessages()

            // Navigate on success message
            if (uiState.publishState == PublishState.SUCCESS) {
                onPublishSuccess()
            }
        }
    }

    // Show dialog for image source selection
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
            Column {
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

                // Tab row for switching between publish and pending
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Connectivity status banner
                ConnectivityStatusBanner(isConnected = uiState.isConnected)
            }
        },
        bottomBar = {
            BottomNavigationBar(currentScreenIndex = selectedItem)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        when (selectedTabIndex) {
            0 -> {
                // Publish book tab
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
                        text = if (uiState.isLoading) "Publishing..." else "Publish",
                        onClick = {
                            viewModel.publishBook()
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth(0.5f),
                        isPrimary = true
                    )
                }
            }
            1 -> {
                // Pending publishes tab
                PendingPublishesTab(
                    pendingPublishes = uiState.pendingPublishes,
                    onRetry = { pendingId -> viewModel.retryPendingPublish(pendingId) },
                    onDelete = { pendingId -> viewModel.deletePendingPublish(pendingId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        ToastHandler(toastState)
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
                text = "You're offline. Books will be published when connectivity is restored.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun PendingPublishesTab(
    pendingPublishes: List<PendingPublishData>,
    onRetry: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (pendingPublishes.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "No pending publishes",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No pending book publishes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // List of pending publishes
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingPublishes) { pendingPublish ->
                    PendingPublishItem(
                        pendingPublish = pendingPublish,
                        onRetry = { onRetry(pendingPublish.id) },
                        onDelete = { onDelete(pendingPublish.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PendingPublishItem(
    pendingPublish: PendingPublishData,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card header with timestamp and buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = orange,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.bodySmall,
                        color = orange,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action buttons
                Row {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Book details
            Text(
                text = pendingPublish.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "by ${pendingPublish.authorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ISBN: ${pendingPublish.isbn}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Show timestamp
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Saved on: ${formatTimestamp(pendingPublish.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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

// Helper function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}