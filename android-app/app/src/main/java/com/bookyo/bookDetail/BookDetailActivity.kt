package com.bookyo.bookDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookyo.components.BookThumbnail
import com.bookyo.components.BookyoButton
import com.bookyo.components.BottomNavigationBar
import com.bookyo.components.ToastHandler
import com.bookyo.components.rememberToastState
import com.bookyo.listing.CreateListingActivity
import com.bookyo.ui.BookyoTheme
import com.bookyo.ui.blue
import com.bookyo.ui.green

class BookDetailActivity : ComponentActivity() {

    private val viewModel: BookDetailViewModel by viewModels {
        BookDetailViewModelFactory(application)
    }

    companion object {
        private const val EXTRA_BOOK_ID = "extra_book_id"

        fun createIntent(context: Context, bookId: String): Intent {
            return Intent(context, BookDetailActivity::class.java).apply {
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
        viewModel.loadBookDetails(bookId)

        setContent {
            BookyoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BookDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = { finish() },
                        onCreateListingClick = {
                            startActivity(CreateListingActivity.createIntent(this, bookId))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: BookDetailViewModel,
    onNavigateBack: () -> Unit,
    onCreateListingClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val toastState = rememberToastState()

    // Show toast messages
    uiState.errorMessage?.let {
        toastState.showError(it)
        viewModel.clearErrorMessage()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Book Details",
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
                ),
                actions = {
                    IconButton(
                        onClick = {
                            toastState.showInfo("Shopping cart not implemented yet")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shopping Cart",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(currentScreenIndex = 0)
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
            } else if (uiState.book != null) {
                // Show book details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Book thumbnail
                    BookThumbnail(
                        thumbnailKey = uiState.book?.thumbnail,
                        modifier = Modifier
                            .size(width = 180.dp, height = 270.dp)
                            .padding(bottom = 16.dp)
                    )

                    // Book title
                    Text(
                        text = uiState.book?.title ?: "",
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Author
                    Text(
                        text = "by ${uiState.authorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ISBN
                    Text(
                        text = "ISBN: ${uiState.book?.isbn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status badge
                    if (uiState.hasListing) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = green.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Listed for Sale: ${uiState.listingPrice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = green,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        // Only show create listing button if the book is not already listed
                        BookyoButton(
                            text = "Create Listing",
                            onClick = onCreateListingClick,
                            modifier = Modifier.padding(vertical = 8.dp),
                            isPrimary = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add to Wishlist button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        BookyoButton(
                            text = if (uiState.isInWishlist) "Remove from Wishlist" else "Add to Wishlist",
                            onClick = {
                                if (uiState.isInWishlist) {
                                    viewModel.removeFromWishlist()
                                } else {
                                    viewModel.addToWishlist()
                                }
                            },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            isPrimary = false
                        )
                    }

                    // Divider
                    Divider(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Book description (placeholder)
                    Text(
                        text = "Book information and description would go here. This section could include categories, tags, ratings, reviews, and a detailed description of the book's content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else if (uiState.errorMessage != null) {
                // Show error message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Failed to load book details",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BookyoButton(
                        text = "Retry",
                        onClick = { viewModel.retryLoadingBook() },
                        isError = true
                    )
                }
            }

            ToastHandler(toastState = toastState)
        }
    }
}