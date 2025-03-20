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




class BookyoApp: Application() {
    override fun onCreate() {
        super.onCreate()

        try {

            val analyticsPlugin = AWSPinpointAnalyticsPlugin()
            Amplify.addPlugin(analyticsPlugin)

            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSApiPlugin())

            Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)

            JavaAmplify.Analytics.recordEvent("APP_START")


            Log.i("BookyoApp", "Initialized Amplify")

        } catch (error: AmplifyException) {
            Log.e("BookyoApp", "Could not initialize Amplify", error)

        }
    }
}