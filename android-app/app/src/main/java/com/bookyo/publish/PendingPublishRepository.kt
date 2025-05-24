package com.bookyo.publish

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
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

// Extension function to create a DataStore in the Context
private val Context.pendingPublishDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pending_publish_requests"
)

/**
 * Serializable data class to hold pending publish data
 */
@Serializable
data class PendingPublishData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isbn: String,
    val authorName: String,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Repository for handling pending book publish requests when offline
 */
class PendingPublishRepository(private val context: Context) {
    private val TAG = "PendingPublishRepo"

    // Keys for DataStore
    private val PENDING_PUBLISH_KEY = stringPreferencesKey("pending_publish_requests")

    /**
     * Save a pending publish request
     */
    suspend fun savePendingPublish(
        title: String,
        isbn: String,
        authorName: String,
        imageUri: Uri?
    ): PendingPublishData {
        // First save the image locally if it exists
        val localImagePath = imageUri?.let { saveImageLocally(it) }

        // Create the pending publish data
        val pendingPublish = PendingPublishData(
            title = title,
            isbn = isbn,
            authorName = authorName,
            imagePath = localImagePath
        )

        // Retrieve current pending list
        val currentPendingList = getPendingPublishes().toMutableList()

        // Add the new request
        currentPendingList.add(pendingPublish)

        // Save the updated list
        context.pendingPublishDataStore.edit { preferences ->
            preferences[PENDING_PUBLISH_KEY] = Json.encodeToString(currentPendingList)
        }

        Log.d(TAG, "Saved pending publish request: $title")
        return pendingPublish
    }

    /**
     * Get all pending publish requests
     */
    suspend fun getPendingPublishes(): List<PendingPublishData> {
        return try {
            context.pendingPublishDataStore.data.map { preferences ->
                val pendingPublishJson = preferences[PENDING_PUBLISH_KEY] ?: return@map emptyList()
                Json.decodeFromString<List<PendingPublishData>>(pendingPublishJson)
            }.map { it.sortedByDescending { publish -> publish.timestamp } }
                .map { it.filter { publish ->
                    // Verify that the image still exists
                    publish.imagePath == null || File(publish.imagePath).exists()
                }}
                .first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending publishes", e)
            emptyList()
        }
    }

    /**
     * Get a flow of pending publish requests
     */
    fun getPendingPublishesFlow(): Flow<List<PendingPublishData>> {
        return context.pendingPublishDataStore.data.map { preferences ->
            try {
                val pendingPublishJson = preferences[PENDING_PUBLISH_KEY] ?: return@map emptyList()
                Json.decodeFromString<List<PendingPublishData>>(pendingPublishJson)
                    .sortedByDescending { it.timestamp }
                    .filter { it.imagePath == null || File(it.imagePath).exists() }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing pending publishes", e)
                emptyList()
            }
        }
    }

    /**
     * Get a specific pending publish by ID
     */
    suspend fun getPendingPublishById(id: String): PendingPublishData? {
        return getPendingPublishes().find { it.id == id }
    }

    /**
     * Remove a pending publish request after it's successfully processed
     */
    suspend fun removePendingPublish(id: String) {
        val currentList = getPendingPublishes().toMutableList()
        val itemToRemove = currentList.find { it.id == id }

        if (itemToRemove != null) {
            // Delete the image file if it exists
            itemToRemove.imagePath?.let { File(it).delete() }

            // Remove from the list
            currentList.remove(itemToRemove)

            // Save the updated list
            context.pendingPublishDataStore.edit { preferences ->
                preferences[PENDING_PUBLISH_KEY] = Json.encodeToString(currentList)
            }

            Log.d(TAG, "Removed pending publish with ID: $id")
        }
    }

    /**
     * Save an image locally for later upload
     */
    private fun saveImageLocally(uri: Uri): String? {
        return try {
            // Create a directory for cached images
            val cacheDir = File(context.cacheDir, "pending_images").apply {
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