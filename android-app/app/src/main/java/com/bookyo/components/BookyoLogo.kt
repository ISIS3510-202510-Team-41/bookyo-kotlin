package com.bookyo.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.bookyo.R


@Composable
fun BookyoLogo(modifier: Modifier) {
    val logoRes = if (isSystemInDarkTheme()) {
        R.drawable.lettermark_white
    } else {
        R.drawable.lettermark_black
    }

    Image(
        painter = painterResource(id = logoRes),
        contentDescription = "Bookyo Logo",
        modifier = modifier
    )
}