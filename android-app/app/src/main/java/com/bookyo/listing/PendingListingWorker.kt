package com.bookyo.listing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.bookyo.utils.ConnectivityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker to process pending listing requests when internet is available
 */
class PendingListingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        private const val TAG = "PendingListingWorker"
        private const val UNIQUE_WORK_NAME = "pending_listing_worker"
        private const val MAX_RETRIES = 3

        // Data keys
        const val KEY_SPECIFIC_ID = "specific_id"
        const val KEY_RETRY_COUNT = "retry_count"

        /**
         * Enqueue work to process all pending listing requests
         */
        fun enqueueWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PendingListingWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, // Initial delay
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )

            Log.d(TAG, "Enqueued work for all pending listings")
        }

        /**
         * Enqueue work to process a specific pending listing request
         */
        fun enqueueSpecificWork(context: Context, pendingId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PendingListingWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_SPECIFIC_ID to pendingId, KEY_RETRY_COUNT to 0))
                .build()

            val uniqueWorkName = "$UNIQUE_WORK_NAME-$pendingId"

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Enqueued work for specific pending listing: $pendingId")
        }
    }

    private lateinit var repository: PendingListingRepository
    private lateinit var listingViewModel: CreateListingViewModel

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting pending listing worker")

        repository = PendingListingRepository(applicationContext)
        listingViewModel = CreateListingViewModel(applicationContext as android.app.Application)

        // Check if we're processing a specific pending listing
        val specificPendingId = inputData.getString(KEY_SPECIFIC_ID)
        val retryCount = inputData.getInt(KEY_RETRY_COUNT, 0)

        return withContext(Dispatchers.IO) {
            val connectivityChecker = ConnectivityChecker(applicationContext)
            if (!connectivityChecker.isConnected()) {
                Log.d(TAG, "No internet connection, retrying later")
                return@withContext Result.retry()
            }

            try {
                val result = if (specificPendingId != null) {
                    processPendingListing(specificPendingId)
                } else {
                    processAllPendingListings()
                }

                if (result) {
                    Result.success()
                } else if (retryCount < MAX_RETRIES) {
                    // Schedule a retry with incremented retry count
                    val newRetryCount = retryCount + 1
                    val outputData = workDataOf(
                        KEY_SPECIFIC_ID to specificPendingId,
                        KEY_RETRY_COUNT to newRetryCount
                    )
                    Log.d(TAG, "Scheduling retry #$newRetryCount for listing: $specificPendingId")
                    Result.failure(outputData)
                } else {
                    Log.e(TAG, "Max retries reached for listing: $specificPendingId")
                    Result.failure()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing pending listings", e)
                if (retryCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }

    private suspend fun processPendingListing(pendingId: String): Boolean {
        val pendingListing = repository.getPendingListingById(pendingId) ?: return false

        Log.d(TAG, "Processing pending listing for book: ${pendingListing.bookId}")

        // Initialize the listing ViewModel with the book ID
        listingViewModel.initialize(pendingListing.bookId)

        return try {
            // Wait for the ViewModel to load the book (with timeout)
            var attempts = 0
            while (listingViewModel.uiState.value.isLoading && attempts < 10) {
                kotlinx.coroutines.delay(500)
                attempts++
            }

            if (listingViewModel.uiState.value.isLoading) {
                Log.e(TAG, "Timeout waiting for book data to load")
                return false
            }

            // Add the images
            val imageUris = pendingListing.imagePaths.mapNotNull { path ->
                repository.getImageUriFromPath(path)
            }

            // If we don't have all the images, abort
            if (imageUris.size != pendingListing.imagePaths.size) {
                Log.e(TAG, "Some images are missing. Expected ${pendingListing.imagePaths.size}, got ${imageUris.size}")
                return false
            }

            // Add each image
            imageUris.forEach { uri ->
                listingViewModel.addListingImage(uri)
            }

            // Set the price and other details
            listingViewModel.updatePrice(pendingListing.price.toString())
            listingViewModel.updateCondition(pendingListing.condition)
            listingViewModel.updateDescription(pendingListing.description)

            // Create the listing
            val result = listingViewModel.createListingSync()

            if (result) {
                // Success - remove the pending listing
                repository.removePendingListing(pendingId)
                Log.d(TAG, "Successfully processed pending listing: $pendingId")
                true
            } else {
                Log.e(TAG, "Failed to process pending listing: $pendingId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing pending listing", e)
            false
        }
    }

    private suspend fun processAllPendingListings(): Boolean {
        val pendingListings = repository.getPendingListings()

        if (pendingListings.isEmpty()) {
            Log.d(TAG, "No pending listings to process")
            return true
        }

        Log.d(TAG, "Processing ${pendingListings.size} pending listings")

        var allSuccessful = true

        for (pendingListing in pendingListings) {
            val success = processPendingListing(pendingListing.id)
            if (!success) {
                // If one fails, mark as unsuccessful but continue processing others
                allSuccessful = false
                Log.d(TAG, "Failed to process listing for book ${pendingListing.bookId}, will retry later")

                // Enqueue a separate worker for this specific pending listing
                PendingListingWorker.enqueueSpecificWork(
                    applicationContext,
                    pendingListing.id
                )
            }
        }

        return allSuccessful
    }
}