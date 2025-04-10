package com.bookyo.notifications

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amplifyframework.api.graphql.model.ModelPagination
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.graphql.model.ModelSubscription
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.datastore.generated.model.Notification
import com.amplifyframework.kotlin.core.Amplify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Timer
import java.util.TimerTask

/**
 * Service to manage notifications throughout the app
 */
class NotificationService(private val application: Application) {
    companion object {
        private const val TAG = "NotificationService"
        private const val POLL_INTERVAL_MS = 60000L // 1 minute

        // Singleton instance
        @Volatile
        private var INSTANCE: NotificationService? = null

        fun getInstance(application: Application): NotificationService {
            return INSTANCE ?: synchronized(this) {
                val instance = NotificationService(application)
                INSTANCE = instance
                instance
            }
        }
    }

    // Coroutine scope for background operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    // Track unread notification count
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // Timer for polling
    private var pollingTimer: Timer? = null

    // Current user
    private var currentUser: AuthUser? = null

    /**
     * Start the notification service
     */
    fun start() {
        Log.d(TAG, "Starting notification service")

        // Check for current user
        serviceScope.launch {
            try {
                currentUser = Amplify.Auth.getCurrentUser()
                Log.d(TAG, "Current user: ${currentUser?.username}")

                // Start polling for notifications
                startPolling()

                // Set up subscription if possible
                setupSubscription()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting notification service", e)
            }
        }
    }

    /**
     * Stop the notification service
     */
    fun stop() {
        Log.d(TAG, "Stopping notification service")
        pollingTimer?.cancel()
        pollingTimer = null
    }

    /**
     * Check for new notifications now
     */
    fun checkNow() {
        serviceScope.launch {
            fetchUnreadCount()
        }
    }

    /**
     * Set up a subscription for real-time notifications
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun setupSubscription() {
        serviceScope.launch {
            try {
                val userId = currentUser?.userId ?: return@launch

                // Subscribe to notifications for this user
                Amplify.API.subscribe(ModelSubscription.onCreate(Notification::class.java))
                    .collect { response ->
                        if (response.hasData()) {
                            val notification = response.data

                            // Check if this notification is for the current user
                            if (notification.recipient == userId || notification.recipient == "*") {
                                Log.d(TAG, "New notification received: ${notification.title}")

                                // Update unread count
                                _unreadCount.value = _unreadCount.value + 1
                            }
                        } else if (response.hasErrors()) {
                            Log.e(TAG, "Subscription error: ${response.errors.first().message}")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up subscription", e)
            }
        }
    }

    /**
     * Start polling for notifications
     */
    private fun startPolling() {
        pollingTimer?.cancel()

        pollingTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    serviceScope.launch {
                        fetchUnreadCount()
                    }
                }
            }, 0, POLL_INTERVAL_MS)
        }
    }

    /**
     * Fetch the unread notification count
     */
    private suspend fun fetchUnreadCount() {
        try {
            val userId = currentUser?.userId ?: return

            val response = Amplify.API.query(
                ModelQuery.list(
                    Notification::class.java,
                    Notification.READ.eq(false)
                        .and(Notification.RECIPIENT.eq(userId).or(Notification.RECIPIENT.eq("*"))),
                    ModelPagination.limit(100)
                )
            )

            val count = response.data.items.count()
            _unreadCount.value = count

            Log.d(TAG, "Unread notification count: $count")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching unread count", e)
        }
    }
}