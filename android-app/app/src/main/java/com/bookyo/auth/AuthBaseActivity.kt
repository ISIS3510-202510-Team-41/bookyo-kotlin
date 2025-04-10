package com.bookyo.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.bookyo.BookyoApp
import com.bookyo.home.HomeScreenActivity
import kotlinx.coroutines.launch

abstract class AuthBaseActivity : ComponentActivity() {
    private val sessionManager = SessionManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkSession()
    }

    private fun checkSession() {
        lifecycleScope.launch {
            when (sessionManager.checkAuthSession()) {
                is SessionManager.AuthState.SignedIn -> {
                    Log.i("AuthBaseActivity", "Active session found, redirecting")
                    navigateToApp()
                }

                is SessionManager.AuthState.SignedOut -> {
                    Log.i("AuthBaseActivity", "No session, showing auth flow")
                    showAuthFlow()
                }
                SessionManager.AuthState.Unknown -> {
                    showAuthFlow()
                }
            }
        }
    }

    private fun navigateToApp() {
        // Start notification service for users with existing sessions
        (application.applicationContext as BookyoApp).startNotificationService()

        val intent = Intent(this, HomeScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    protected abstract fun showAuthFlow()
}