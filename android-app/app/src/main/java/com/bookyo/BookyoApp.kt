package com.bookyo

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.analytics.pinpoint.AWSPinpointAnalyticsPlugin
import com.amplifyframework.core.Amplify as JavaAmplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.bookyo.notifications.NotificationService

class BookyoApp: Application() {

    // Notification service
    lateinit var notificationService: NotificationService

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


            notificationService = NotificationService.getInstance(this)



        } catch (error: AmplifyException) {
            Log.e("BookyoApp", "Could not initialize Amplify", error)
        }
    }

    fun startNotificationService() {
        try {
            Log.i("BookyoApp", "Starting notification service after successful login")
            notificationService.start()
        } catch (e: Exception) {
            Log.e("BookyoApp", "Failed to start notification service", e)
        }
    }
}