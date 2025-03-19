package com.bookyo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightModeScheme = lightColorScheme(
    primary = green,
    onPrimary = black,
    primaryContainer = whiteGray,
    secondaryContainer = whiteGray,
    onPrimaryContainer = lightGray,
    surface = white,
    onSurface = black,
    onSurfaceVariant = lightGray,
    secondary = blue,
    onSecondary = white,
    tertiary = orange,
    onTertiary = lightGray,
    error = red,
    onError = white,
    outline = lightGray,
    outlineVariant = blue,
)

private val DarkModeScheme = darkColorScheme(
    primary = green,
    onPrimary = black,
    primaryContainer = blueGray,
    secondaryContainer = blueGray,
    onPrimaryContainer = lightGray,
    surface = black,
    onSurface = white,
    onSurfaceVariant = lightGray,
    secondary = orange,
    onSecondary = white,
    tertiary = blue,
    onTertiary = lightGray,
    error = red,
    onError = white,
    outline = lightGray,
    outlineVariant = blue,
)

@Composable
fun BookyoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (!darkTheme) {
            LightModeScheme
        } else {
            DarkModeScheme
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

