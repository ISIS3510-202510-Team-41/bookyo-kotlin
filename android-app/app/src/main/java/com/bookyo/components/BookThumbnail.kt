package com.bookyo.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bookyo.R
import com.bookyo.services.ImageLoadingState
import com.bookyo.services.rememberImageLoader
import com.bookyo.ui.*


@Composable
fun BookThumbnail(
    thumbnailKey: String?, modifier: Modifier = Modifier
) {
    var imageState by remember { mutableStateOf<ImageLoadingState>(ImageLoadingState.Loading) }
    val imageLoader = rememberImageLoader(LocalContext.current)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.67f)
            .background(whiteGray),
        contentAlignment = Alignment.Center
    ) {
        when {
            thumbnailKey == null -> {
                Icon(
                    painter = painterResource(R.drawable.visibility_24px),
                    contentDescription = null,
                    tint = blue
                )
            }

            imageState is ImageLoadingState.Loading -> {
                CircularProgressIndicator(color = blue)
            }

            imageState is ImageLoadingState.Success -> {
                Image(
                    bitmap = (imageState as ImageLoadingState.Success).bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            imageState is ImageLoadingState.Error -> {
                Icon(
                    painter = painterResource(R.drawable.visibility_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    LaunchedEffect(thumbnailKey) {
        thumbnailKey?.let {
            imageLoader.loadImage(it).collect { state ->
                imageState = state
            }
        }
    }
}