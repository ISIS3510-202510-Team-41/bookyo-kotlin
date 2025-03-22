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


class ImageLoader(private val context: Context) {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    suspend fun loadImage(key: String): Flow<ImageLoadingState> = flow {
        emit(ImageLoadingState.Loading)

            // Download from S3
            var tempFile = File(context.cacheDir, "images/$key")
            val dl = Amplify.Storage.downloadFile(StoragePath.fromString("images/$key"), tempFile)
            val file = dl.result().file


            // Decode and optimize bitmap
            val bitmap = withContext(Dispatchers.IO) {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.path, options)

                options.inSampleSize = calculateInSampleSize(options, 300, 300)
                options.inJustDecodeBounds = false

                BitmapFactory.decodeFile(file.path, options)
            }

            // Clean up file
            file.delete()

            emit(ImageLoadingState.Success(bitmap))
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun String.hashKey(): String {
        return MessageDigest.getInstance("MD5").digest(toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}

sealed class ImageLoadingState {
    object Loading : ImageLoadingState()
    data class Success(val bitmap: Bitmap) : ImageLoadingState()
    data class Error(val exception: Exception) : ImageLoadingState()
}

@Composable
fun rememberImageLoader(context: Context): ImageLoader {
    return remember { ImageLoader(context) }
}