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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookyo.R
import com.bookyo.components.BottomNavigationBar
import com.bookyo.home.HomeScreenActivity
import com.bookyo.components.Navigation
import com.bookyo.components.rememberToastState
import com.bookyo.ui.BookyoTheme
import com.bookyo.ui.blue
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


class SearchScreenActivity: ComponentActivity() {

    private val viewModel: SearchScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchScreenViewModel) {
    val toastState = rememberToastState()
    val currentScreenIndex = Navigation.getSelectedIndexForActivity(HomeScreenActivity::class.java)

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Listings", "Books")
    val tabIcons = listOf(R.drawable.ic_book, R.drawable.ic_book)

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

        val listState = rememberLazyListState()

        val items = if (selectedTabIndex == 0) {
            val books = viewModel.books.collectAsState().value
            books.map { it.title }
        } else {
            val listings = viewModel.listings.collectAsState().value
            listings.map { "${it.book} - ${it.price}" }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item)
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SearchScreenPreview() {
    val mockViewModel = SearchScreenViewModel()

    BookyoTheme {
        SearchScreen(mockViewModel)
    }
}
