package com.bookyo.publish

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.workDataOf
import androidx.work.Constraints
import androidx.work.NetworkType
import com.bookyo.utils.ConnectivityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager Worker to process pending book publish requests when internet is available
 */
class PendingPublishWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        private const val TAG = "PendingPublishWorker"
        private const val UNIQUE_WORK_NAME = "pending_publish_worker"

        // Data keys
        const val KEY_SPECIFIC_ID = "specific_id"

        /**
         * Enqueue work to process all pending publish requests
         */
        fun enqueueWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PendingPublishWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Enqueued work to process pending publishes")
        }

        /**
         * Enqueue work to process a specific pending publish request
         */
        fun enqueueSpecificWork(context: Context, pendingId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PendingPublishWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_SPECIFIC_ID to pendingId))
                .build()

            val uniqueWorkName = "$UNIQUE_WORK_NAME-$pendingId"

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Enqueued work for specific pending publish: $pendingId")
        }
    }

    private lateinit var repository: PendingPublishRepository
    private lateinit var publishViewModel: PublishViewModel

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting pending publish worker")

        repository = PendingPublishRepository(applicationContext)
        publishViewModel = PublishViewModel(applicationContext as android.app.Application)

        // Check if we're processing a specific pending publish
        val specificPendingId = inputData.getString(KEY_SPECIFIC_ID)

        return withContext(Dispatchers.IO) {
            val connectivityChecker = ConnectivityChecker(applicationContext)
            if (!connectivityChecker.isConnected()) {
                Log.d(TAG, "No internet connection, retrying later")
                return@withContext Result.retry()
            }

            try {
                if (specificPendingId != null) {
                    processPendingPublish(specificPendingId)
                } else {
                    processAllPendingPublishes()
                }
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing pending publishes", e)
                Result.retry()
            }
        }
    }

    private suspend fun processPendingPublish(pendingId: String): Boolean {
        val pendingPublish = repository.getPendingPublishById(pendingId) ?: return false

        Log.d(TAG, "Processing pending publish: ${pendingPublish.title}")

        return try {
            // Prepare data for the publish
            publishViewModel.title = pendingPublish.title
            publishViewModel.isbn = pendingPublish.isbn
            publishViewModel.authorName = pendingPublish.authorName

            // Set the image URI if available
            if (pendingPublish.imagePath != null) {
                val imageUri = repository.getImageUriFromPath(pendingPublish.imagePath)
                if (imageUri != null) {
                    publishViewModel.selectedImageUri = imageUri
                } else {
                    Log.e(TAG, "Failed to get image URI for path: ${pendingPublish.imagePath}")
                    return false
                }
            }

            // Call the publish method
            val publishResult = publishViewModel.publishBookSync()

            if (publishResult) {
                Log.d(TAG, "Successfully published book: ${pendingPublish.title}")
                repository.removePendingPublish(pendingId)
                true
            } else {
                Log.e(TAG, "Failed to publish book: ${pendingPublish.title}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing pending publish: ${pendingPublish.title}", e)
            false
        }
    }

    private suspend fun processAllPendingPublishes() {
        val pendingPublishes = repository.getPendingPublishes()

        if (pendingPublishes.isEmpty()) {
            Log.d(TAG, "No pending publishes to process")
            return
        }

        Log.d(TAG, "Processing ${pendingPublishes.size} pending publishes")

        for (pendingPublish in pendingPublishes) {
            val success = processPendingPublish(pendingPublish.id)
            if (!success) {
                // If one fails, we'll try again later
                Log.d(TAG, "Failed to process ${pendingPublish.title}, will retry later")
            }
        }
    }
}