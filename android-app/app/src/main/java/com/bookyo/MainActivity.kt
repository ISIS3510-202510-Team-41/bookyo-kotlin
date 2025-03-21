package com.bookyo

import android.content.Intent

import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent

import com.bookyo.auth.login.LoginActivity

import com.bookyo.ui.BookyoTheme

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BookyoTheme {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
}

