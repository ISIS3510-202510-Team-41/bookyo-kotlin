package com.bookyo.searchFeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bookyo.R
import com.bookyo.components.BottomNavigationBar
import com.bookyo.components.Navigation
import com.bookyo.components.ToastHandler
import com.bookyo.components.rememberToastState
import com.bookyo.ui.BookyoTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.bookyo.bookDetail.BookDetailActivity

class SearchScreenActivity: ComponentActivity() {

    private val viewModel: SearchScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookyoTheme {
                SearchScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchScreenViewModel) {
    val toastState = rememberToastState()
    val currentScreenIndex = Navigation.getSelectedIndexForActivity(SearchScreenActivity::class.java)
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Listings", "Books")
    val tabIcons = listOf(R.drawable.ic_shopping_cart, R.drawable.ic_book)

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Bookyo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                toastState.showInfo("Profile not implemented yet")
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                toastState.showInfo("Shopping cart not implemented yet")
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shopping_cart),
                                contentDescription = "Shopping Cart",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) },
                            icon = {
                                Icon(
                                    painter = painterResource(id = tabIcons[index]),
                                    contentDescription = title
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(currentScreenIndex = currentScreenIndex)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is SearchScreenUIState.Loading -> {
                    // Loading state
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is SearchScreenUIState.Success -> {
                    val successState = uiState as SearchScreenUIState.Success
                    val listState = rememberLazyListState()

                    // Check if we should display empty state
                    val displayBooks = selectedTabIndex == 1 && successState.books.isNotEmpty()
                    val displayListings = selectedTabIndex == 0 && successState.books.any { it.isListed } &&
                            successState.listings != null && successState.listings.isNotEmpty()

                    if (!displayBooks && !displayListings) {
                        // Empty state for current tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No ${if (selectedTabIndex == 0) "listings" else "books"} found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Content state - show books or listings based on selected tab
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedTabIndex == 0 && successState.listings != null) {
                                // Listings tab
                                val listedBooks = successState.books.filter { it.isListed }
                                items(listedBooks) { book ->
                                    // Find matching listing by ID
                                    val listing = successState.listings.find { it.id == book.id }
                                    if (listing != null) {
                                        BookCard(
                                            book = book,
                                            listing = listing,
                                            onClick = {
                                                context.startActivity(
                                                    BookDetailActivity.createIntent(context, book.id)
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                // Books tab
                                items(successState.books) { book ->
                                    BookCard(
                                        book = book,
                                        onClick = {
                                            context.startActivity(
                                                BookDetailActivity.createIntent(context, book.id)
                                            )
                                        }
                                    )
                                }
                            }

                            // Loading more indicator
                            if (successState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is SearchScreenUIState.Error -> {
                    val errorState = uiState as SearchScreenUIState.Error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (errorState) {
                                is SearchScreenUIState.Error.Network -> "Network error. Please check your connection."
                                is SearchScreenUIState.Error.Generic -> errorState.message
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                when (errorState) {
                                    is SearchScreenUIState.Error.Network -> errorState.retry()
                                    is SearchScreenUIState.Error.Generic -> errorState.retry()
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }

                SearchScreenUIState.Empty -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Toast handler
            ToastHandler(toastState)
        }
    }
}