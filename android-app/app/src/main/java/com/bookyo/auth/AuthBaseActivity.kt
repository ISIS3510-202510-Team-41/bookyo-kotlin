package com.bookyo.auth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
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
        //TODO()
    }

    protected abstract fun showAuthFlow()
}