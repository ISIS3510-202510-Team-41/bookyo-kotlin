package com.bookyo

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.analytics.pinpoint.AWSPinpointAnalyticsPlugin
import com.amplifyframework.core.Amplify as JavaAmplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.bookyo.notifications.NotificationService
import com.bookyo.publish.PendingPublishWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BookyoApp: Application(), Configuration.Provider {

    // Notification service
    lateinit var notificationService: NotificationService

    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Amplify plugins
            val analyticsPlugin = AWSPinpointAnalyticsPlugin()
            Amplify.addPlugin(analyticsPlugin)

            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSApiPlugin())

            Log.d("BookyoApp", "Attempting to load Amplify config from: ${R.raw.amplify_outputs}")
            val configFile = resources.openRawResource(R.raw.amplify_outputs)
            val size = configFile.available()
            Log.d("BookyoApp", "Config file size: $size bytes")
            configFile.close()

            Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)
            JavaAmplify.Analytics.recordEvent("APP_START")

            Log.i("BookyoApp", "Initialized Amplify")

            // Initialize notification service
            notificationService = NotificationService.getInstance()

            // Initialize WorkManager for background tasks
            WorkManager.getInstance(this)

            // Check auth state and pending publishes in a coroutine
            applicationScope.launch {
                checkAuthAndPendingPublishes()
            }
        } catch (error: AmplifyException) {
            Log.e("BookyoApp", "Could not initialize Amplify", error)
        }
    }

    private suspend fun checkAuthAndPendingPublishes() {
        try {
            // Properly call suspend function within a coroutine
            val session = Amplify.Auth.fetchAuthSession()
            if (session.isSignedIn) {
                Log.d("BookyoApp", "User is signed in, checking pending publishes")
                checkPendingPublishes()
            } else {
                Log.d("BookyoApp", "User is not signed in")
            }
        } catch (e: Exception) {
            Log.e("BookyoApp", "Error checking auth session", e)
        }
    }

    private fun checkPendingPublishes() {
        // Schedule a worker to process any pending publish requests
        PendingPublishWorker.enqueueWork(this)
    }

    fun startNotificationService() {
        try {
            Log.i("BookyoApp", "Starting notification service after successful login")
            notificationService.start()
        } catch (e: Exception) {
            Log.e("BookyoApp", "Failed to start notification service", e)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    }
}