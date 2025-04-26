package com.bookyo.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bookyo.auth.login.LoginActivity
import com.bookyo.components.BottomNavigationBar
import com.bookyo.components.BookyoButton
import com.bookyo.components.ToastHandler
import com.bookyo.components.rememberToastState
import com.bookyo.ui.BookyoTheme

class ProfileScreenActivity : ComponentActivity() {
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookyoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onLogoutSuccess = {
                            // Navigate to login screen when logout is successful
                            val intent = Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
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
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastState = rememberToastState()

    // Show error message as toast if present
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            toastState.showError(it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(currentScreenIndex = 4) // Profile is index 4
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // User info section
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                uiState.user?.let { user ->
                    // Display user information
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${user.firstName} ${user.lastName}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!user.phone.isNullOrEmpty()) {
                                Text(
                                    text = user.phone,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (!user.address.isNullOrEmpty()) {
                                Text(
                                    text = user.address,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = "User information not available",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout button at the bottom
            BookyoButton(
                text = if (uiState.isLoggingOut) "Logging out..." else "Logout",
                onClick = { viewModel.logout(onLogoutSuccess) },
                enabled = !uiState.isLoggingOut,
                isError = true,
                modifier = Modifier.fillMaxWidth(0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        ToastHandler(toastState = toastState)
    }
}