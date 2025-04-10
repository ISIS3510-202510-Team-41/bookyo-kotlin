package com.bookyo.publish

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.datastore.generated.model.Author
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.datastore.generated.model.Notification
import com.amplifyframework.datastore.generated.model.NotificationType
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.storage.StoragePath
import com.bookyo.analytics.BookyoAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class PublishState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

data class PublishUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val publishState: PublishState = PublishState.IDLE,
    val imageUri: Uri? = null,
    val isbn: String = "",
    val title: String = "",
    val authorName: String = "",
)

class PublishViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "PublishViewModel"

    private val _uiState = MutableStateFlow(PublishUIState())
    val uiState: StateFlow<PublishUIState> = _uiState.asStateFlow()

    // Mutable properties for the view to update
    var isbn by mutableStateOf("")
    var title by mutableStateOf("")
    var authorName by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)

    fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri
        Log.d(TAG, "Image Selected")
    }

    fun validateForm(): String? {
        return when {
            isbn.isBlank() -> "ISBN is required"
            title.isBlank() -> "Title is required"
            authorName.isBlank() -> "Author name is required"
            selectedImageUri == null -> "Book image is required"
            else -> null
        }
    }

    fun publishBook() {
        val validationError = validateForm()
        if (validationError != null) {
            setErrorState(validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )

            val start = System.currentTimeMillis()
            Log.d(TAG, "Starting image upload")

            val imageKey = selectedImageUri?.let { uri ->
                try {
                    "${UUID.randomUUID()}.jpg".also { key ->
                        uploadImage(key, uri)
                        Log.d(TAG, "Uploaded Image successfully")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to upload image", e)
                    null
                }
            }

            try {
                val authorEntity = try {
                    checkAuthorByName(authorName) ?: run {
                        val newAuthor = Author.builder().name(authorName).build()
                        Amplify.API.mutate(ModelMutation.create(newAuthor)).data.also {
                            Log.d(TAG, "Created new author: ${it.name}")
                        }
                    }
                } catch (e: Exception) {
                    throw Exception("Failed to process author: ${e.message}")
                }

                val book = Book.builder().title(title).isbn(isbn).author(authorEntity).apply {
                    imageKey?.let { thumbnail(it) }
                }.build()

                val createBookResult = Amplify.API.mutate(ModelMutation.create(book))
                if (createBookResult.hasErrors()) {
                    throw Exception("Error creating book: ${createBookResult.errors.first().message}")
                }

                Log.d(TAG, "Successfully created book: ${book.title}")

                // Create notification about the new book
                try {
                    val notification = Notification.builder()
                        .title("New Book Available")
                        .body("\"${book.title}\" by ${authorEntity.name} is now available!")
                        .recipient("*") // Broadcast to all users
                        .read(false)
                        .type(NotificationType.NEW_BOOK)
                        .build()

                    val createNotificationResult = Amplify.API.mutate(ModelMutation.create(notification))
                    if (createNotificationResult.hasErrors()) {
                        Log.e(TAG, "Error creating notification: ${createNotificationResult.errors.first().message}")
                    } else {
                        Log.d(TAG, "Created notification about new book")
                    }
                } catch (e: Exception) {
                    // Just log the error - don't fail the book creation
                    Log.e(TAG, "Failed to create notification", e)
                }

                BookyoAnalytics.trackApiCall(
                    endpoint = "createBook",
                    isSuccess = true,
                    durationMs = System.currentTimeMillis() - start
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    publishState = PublishState.SUCCESS,
                    successMessage = "Book published successfully!"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to create book", e)
                BookyoAnalytics.trackApiCall(
                    endpoint = "createBook",
                    isSuccess = false,
                    durationMs = System.currentTimeMillis() - start,
                    errorType = e.javaClass.simpleName,
                    errorMessage = e.message
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    publishState = PublishState.ERROR,
                    errorMessage = "Failed to publish book: ${e.message}"
                )
            }
        }
    }

    private suspend fun checkAuthorByName(name: String): Author? {
        try {
            val res = Amplify.API.query(
                ModelQuery.list(Author::class.java, Author.NAME.eq(name))
            )

            if (res.hasErrors()) {
                throw Exception("Error querying authors: ${res.errors.first().message}")
            }

            return res.data.items.firstOrNull()?.let { author ->
                Log.d(TAG, "Found author $name")
                author
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check author", e)
            throw Exception("Failed to check author $name: ${e.message}")
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun uploadImage(key: String, uri: Uri): String {
        return try {
            val contentResolver = getApplication<Application>().contentResolver

            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("upload", ".jpg")
            }.apply {
                deleteOnExit()
            }

            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Failed to read image content")

            val upload = Amplify.Storage.uploadFile(
                StoragePath.fromString("images/$key"), tempFile
            )

            val result = upload.result()
            tempFile.delete()

            Log.d(TAG, "Successfully uploaded image: $key")
            key
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    private fun setErrorState(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = errorMessage,
            publishState = PublishState.ERROR
        )
    }

    fun resetState() {
        _uiState.value = PublishUIState()
        isbn = ""
        title = ""
        authorName = ""
        selectedImageUri = null
    }
}