package com.bookyo.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.datastore.generated.model.User
import com.amplifyframework.kotlin.core.Amplify
import com.bookyo.BookyoApp
import com.bookyo.auth.AmplifyAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUIState(
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val user: User? = null,
    val authUser: AuthUser? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val authManager = AmplifyAuthManager()
    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val authUser = Amplify.Auth.getCurrentUser()
                _uiState.update { it.copy(authUser = authUser) }

                // Fetch user details from database
                fetchUserDetails(authUser.username)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching current user", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to get user information"
                    )
                }
            }
        }
    }

    private suspend fun fetchUserDetails(email: String) {
        try {
            val response = Amplify.API.query(
                ModelQuery.list(
                    User::class.java,
                    User.EMAIL.eq(email)
                )
            )

            val user = response.data.items.firstOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user details", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load user details"
                )
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }

            try {
                // Stop notification service
                (getApplication<Application>() as? BookyoApp)?.notificationService?.stop()

                // Sign out from Amplify
                authManager.signOut().onSuccess {
                    Log.i(TAG, "Successfully signed out")
                    onLogoutSuccess()
                }.onFailure { error ->
                    Log.e(TAG, "Sign out failed", error)
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            errorMessage = "Failed to log out: ${error.localizedMessage}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        errorMessage = "Failed to log out: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}