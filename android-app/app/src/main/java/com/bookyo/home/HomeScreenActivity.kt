package com.bookyo.home

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import com.bookyo.ui.BookyoTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.bookyo.components.BookyoButton
import com.bookyo.components.Navigation
import com.bookyo.components.BottomNavigationBar
import com.bookyo.ui.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.bookyo.R
import com.bookyo.components.rememberToastState

class HomeScreenActivity: ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BookyoTheme {
                HomeScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {

    // Collect books as state
    val books by viewModel.books.collectAsState()
    val toastState = rememberToastState()

    // Get the current screen index
    val currentScreenIndex = Navigation.getSelectedIndexForActivity(HomeScreenActivity::class.java)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Home",
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
                        androidx.compose.material3.Icon(
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // Spacing between elements
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Container for spaces 1 and 2
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Distributes space between Toolbar and NavigationBar
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Space 1 - Image + Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp, 170.dp)
                            .background(whiteGray)
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(books.size) { index ->
                                // Create a composable for each book item
                                Card(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(150.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = whiteGray)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = books[index].title,
                                            style = typography.displaySmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Add author if available
                                        books[index].author?.let {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "by $it",
                                                style = typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BookyoButton(
                        onClick = { /* Action for exploring books */ },
                        modifier = Modifier.fillMaxWidth(0.8f), // Adjust button size
                        text = "Browse Books"
                    )

                    Spacer(modifier = Modifier.height(40.dp)) // Space between elements

                    // Space 2 - Image + Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(300.dp, 170.dp)
                                .background(whiteGray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BookyoButton(
                            onClick = { /* Action for publishing a book */ },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            text = "Publish Book"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    BookyoTheme {
        HomeScreen(HomeViewModel())
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHomeScreenDark() {
    BookyoTheme {
        HomeScreen(HomeViewModel())
    }
}






