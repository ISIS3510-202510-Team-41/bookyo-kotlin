package com.bookyo.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.result.StorageGetUrlResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bookyo.R
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import com.bookyo.services.ImageLoader
import com.bookyo.services.rememberImageLoader
import com.bookyo.ui.blue
import com.bookyo.ui.white


@Composable
fun BookThumbnail(
    thumbnailKey: String?,  // Clave del archivo en S3, ejemplo: "book123.jpg"
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var imageState by remember { mutableStateOf<ImageLoader.ImageLoadingState>(ImageLoader.ImageLoadingState.Loading) }

    val imageLoader = rememberImageLoader(context)

    // Se dispara cada vez que cambia thumbnailKey
    LaunchedEffect(thumbnailKey) {
        if (!thumbnailKey.isNullOrEmpty()) {
            coroutineScope.launch {
                imageState = ImageLoader.ImageLoadingState.Loading
                try {
                    imageLoader.loadImage(thumbnailKey)
                        .collect { state ->
                            imageState = state
                        }
                } catch (e: Exception) {
                    imageState = ImageLoader.ImageLoadingState.Error(e)
                    Log.e("BookThumbnail", "Error al cargar la imagen", e)
                }
            }
        } else {
            imageState = ImageLoader.ImageLoadingState.Error(Exception("Clave de imagen inválida"))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.67f)
            .background(white),
        contentAlignment = Alignment.Center
    ) {
        when (imageState) {
            is ImageLoader.ImageLoadingState.Loading -> {
                CircularProgressIndicator(color = blue)
            }

            is ImageLoader.ImageLoadingState.Error -> {
                Icon(
                    painter = painterResource(R.drawable.ic_book),
                    contentDescription = "Error al cargar la imagen",
                    tint = blue,
                    modifier = Modifier.size(48.dp)
                )
            }

            is ImageLoader.ImageLoadingState.Success -> {
                val bitmap = (imageState as ImageLoader.ImageLoadingState.Success).bitmap
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Miniatura del libro",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }
    }
}
