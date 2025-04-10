package com.bookyo.notifications

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelPagination
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.datastore.generated.model.Notification
import com.amplifyframework.kotlin.core.Amplify
import com.bookyo.analytics.BookyoAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "NotificationsViewModel"
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentUser: AuthUser? = null

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                currentUser = Amplify.Auth.getCurrentUser()
                fetchNotifications()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching current user", e)
                _errorMessage.value = "Please sign in to view notifications"
            }
        }
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val notifications = loadNotifications()
                _notifications.value = notifications
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching notifications", e)
                _errorMessage.value = "Failed to load notifications"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadNotifications(): List<Notification> = supervisorScope {
        val start = System.currentTimeMillis()

        try {
            val userId = currentUser?.userId ?: throw Exception("User not authenticated")

            val response = Amplify.API.query(
                ModelQuery.list(
                    Notification::class.java,
                    Notification.RECIPIENT.eq(userId).or(Notification.RECIPIENT.eq("*")),
                    ModelPagination.limit(50)
                )
            )

            val duration = System.currentTimeMillis() - start
            BookyoAnalytics.trackApiCall(
                "loadNotifications",
                true,
                duration,
                null,
                null,
                null
            )

            return@supervisorScope response.data.items.toList().sortedByDescending {
                if (!it.read) 1 else 0
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - start
            BookyoAnalytics.trackApiCall(
                "loadNotifications",
                false,
                duration,
                e.javaClass.simpleName,
                e.message,
                null
            )
            throw e
        }
    }

    fun markAsRead(notification: Notification) {
        viewModelScope.launch {
            try {
                val updatedNotification = notification.copyOfBuilder()
                    .read(true)
                    .build()

                Amplify.API.mutate(ModelMutation.update(updatedNotification))

                val currentList = _notifications.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == notification.id }
                if (index >= 0) {
                    currentList[index] = updatedNotification
                    _notifications.value = currentList
                }

                Log.d(TAG, "Marked notification as read: ${notification.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
                _errorMessage.value = "Failed to update notification"
            }
        }
    }

    fun getUnreadCount(): Int {
        return _notifications.value.count { !it.read }
    }
}