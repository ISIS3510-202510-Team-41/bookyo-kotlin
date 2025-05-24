package com.bookyo.auth


import android.util.Log
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.kotlin.core.Amplify

class SessionManager {
    companion object {
        private const val TAG = "SessionManager"
    }


    suspend fun checkAuthSession(): AuthState {
        return try {
            Log.d(TAG, "Checking current auth session")
            val session = Amplify.Auth.fetchAuthSession()

            if (session.isSignedIn) {
                try {
                    val user = Amplify.Auth.getCurrentUser()
                    Log.i(TAG, "User is signed in: ${user.username.masked()}")
                    AuthState.SignedIn(user)
                } catch (e: Exception) {
                    Log.w(TAG, "Session claims user is signed in but getCurrentUser failed", e)
                    AuthState.SignedOut
                }
            } else {
                Log.i(TAG, "No active session found")
                AuthState.SignedOut
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking auth session", e)
            AuthState.SignedOut
        }
    }

    sealed class AuthState {
        object Unknown : AuthState()
        object SignedOut : AuthState()
        data class SignedIn(val user: AuthUser) : AuthState()
    }

    private fun String.masked() = "${take(2)}***${takeLast(2)}"
}