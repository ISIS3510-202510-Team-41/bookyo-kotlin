package com.bookyo

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin

import com.example.bookyo.R

class BooykoApp: Application() {
    override fun onCreate() {
        super.onCreate()

        try {

            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSApiPlugin())

            Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)
            Log.i("BookyoApp", "Initialized Amplify")

        } catch (error: AmplifyException) {
            Log.e("BookyoApp", "Could not initialize Amplify", error)

        }
    }
}