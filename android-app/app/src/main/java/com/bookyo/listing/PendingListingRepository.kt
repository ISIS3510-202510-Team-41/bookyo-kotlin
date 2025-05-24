package com.bookyo.listing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

// Extension function to create a DataStore in the Context
private val Context.pendingListingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pending_listing_requests"
)

/**
 * Serializable data class to hold pending listing data
 */
@Serializable
data class PendingListingData(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val price: Double,
    val imagePaths: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Repository for handling pending listing requests when offline
 */
class PendingListingRepository(private val context: Context) {
    private val TAG = "PendingListingRepo"

    // Keys for DataStore
    private val PENDING_LISTING_KEY = stringPreferencesKey("pending_listing_requests")

    /**
     * Save a pending listing request
     */
    suspend fun savePendingListing(
        bookId: String,
        price: Double,
        images: List<Uri> = emptyList()
    ): PendingListingData {
        // First save the images locally if they exist
        val localImagePaths = images.mapNotNull { uri ->
            saveImageLocally(uri)
        }

        // Create the pending listing data
        val pendingListing = PendingListingData(
            bookId = bookId,
            price = price,
            imagePaths = localImagePaths
        )

        // Retrieve current pending list
        val currentPendingList = getPendingListings().toMutableList()

        // Add the new request
        currentPendingList.add(pendingListing)

        // Save the updated list
        context.pendingListingDataStore.edit { preferences ->
            preferences[PENDING_LISTING_KEY] = Json.encodeToString(currentPendingList)
        }

        Log.d(TAG, "Saved pending listing request for book: $bookId")
        return pendingListing
    }

    /**
     * Get all pending listing requests
     */
    suspend fun getPendingListings(): List<PendingListingData> {
        return try {
            context.pendingListingDataStore.data.map { preferences ->
                val pendingListingJson = preferences[PENDING_LISTING_KEY] ?: return@map emptyList()
                Json.decodeFromString<List<PendingListingData>>(pendingListingJson)
            }.map { it.sortedByDescending { listing -> listing.timestamp } }
                .map { it.filter { listing ->
                    // Verify that all images still exist
                    listing.imagePaths.all { imagePath -> File(imagePath).exists() }
                }}
                .first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending listings", e)
            emptyList()
        }
    }

    /**
     * Get a flow of pending listing requests
     */
    fun getPendingListingsFlow(): Flow<List<PendingListingData>> {
        return context.pendingListingDataStore.data.map { preferences ->
            try {
                val pendingListingJson = preferences[PENDING_LISTING_KEY] ?: return@map emptyList()
                Json.decodeFromString<List<PendingListingData>>(pendingListingJson)
                    .sortedByDescending { it.timestamp }
                    .filter { listing ->
                        listing.imagePaths.all { imagePath -> File(imagePath).exists() }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing pending listings", e)
                emptyList()
            }
        }
    }

    /**
     * Get a specific pending listing by ID
     */
    suspend fun getPendingListingById(id: String): PendingListingData? {
        return getPendingListings().find { it.id == id }
    }

    /**
     * Remove a pending listing request after it's successfully processed
     */
    suspend fun removePendingListing(id: String) {
        val currentList = getPendingListings().toMutableList()
        val itemToRemove = currentList.find { it.id == id }

        if (itemToRemove != null) {
            // Delete the image files if they exist
            itemToRemove.imagePaths.forEach { File(it).delete() }

            // Remove from the list
            currentList.remove(itemToRemove)

            // Save the updated list
            context.pendingListingDataStore.edit { preferences ->
                preferences[PENDING_LISTING_KEY] = Json.encodeToString(currentList)
            }

            Log.d(TAG, "Removed pending listing with ID: $id")
        }
    }

    /**
     * Remove all pending listing requests
     */
    suspend fun clearAllPendingListings() {
        // Delete all saved images
        getPendingListings().forEach { pendingListing ->
            pendingListing.imagePaths.forEach { File(it).delete() }
        }

        // Clear the preferences
        context.pendingListingDataStore.edit { preferences ->
            preferences[PENDING_LISTING_KEY] = Json.encodeToString(emptyList<PendingListingData>())
        }

        Log.d(TAG, "Cleared all pending listings")
    }

    /**
     * Save an image locally for later upload
     */
    private fun saveImageLocally(uri: Uri): String? {
        return try {
            // Create a directory for cached images
            val cacheDir = File(context.cacheDir, "pending_listing_images").apply {
                if (!exists()) mkdirs()
            }

            // Create a file for the image
            val imageFile = File(cacheDir, "img_${System.currentTimeMillis()}.jpg")

            // Copy the content from the URI to the file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Could not open input stream")

            Log.d(TAG, "Saved image locally at: ${imageFile.absolutePath}")
            imageFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image locally", e)
            null
        }
    }

    /**
     * Get a Uri from a saved image path
     */
    fun getImageUriFromPath(path: String): Uri? {
        val file = File(path)
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            Log.e(TAG, "Image file does not exist: $path")
            null
        }
    }
}