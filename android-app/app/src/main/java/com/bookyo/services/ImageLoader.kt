package com.bookyo.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.storage.StoragePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import com.bumptech.glide.Glide


class ImageLoader(private val context: Context) {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    suspend fun loadImage(key: String): Flow<ImageLoadingState> = flow {
        emit(ImageLoadingState.Loading)

        try {

            // Download from S3
            val tempFile = File(context.cacheDir, "images/$key")
            val download = Amplify.Storage.downloadFile(StoragePath.fromString("images/$key"), tempFile)
            val file = download.result().file

            // Load bitmap with Glide
            val bitmap = withContext(Dispatchers.IO) {
                Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .submit()
                    .get()
            }

            emit(ImageLoadingState.Success(bitmap))
        } catch (e: Exception) {
            emit(ImageLoadingState.Error(e))
        }
    }

sealed class ImageLoadingState {
    object Loading : ImageLoadingState()
    data class Success(val bitmap: Bitmap) : ImageLoadingState()
    data class Error(val exception: Exception) : ImageLoadingState()
}
}

@Composable
fun rememberImageLoader(context: Context): ImageLoader {
    return remember { ImageLoader(context) }
}