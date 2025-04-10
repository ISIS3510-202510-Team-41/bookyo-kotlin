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

            Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)
            JavaAmplify.Analytics.recordEvent("APP_START")

            Log.i("BookyoApp", "Initialized Amplify")

            // Initialize and start notification service
            notificationService = NotificationService.getInstance(this)
            notificationService.start()

        } catch (error: AmplifyException) {
            Log.e("BookyoApp", "Could not initialize Amplify", error)
        }
    }
}