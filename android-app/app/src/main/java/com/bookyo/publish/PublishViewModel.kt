package com.bookyo.publish

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.ApiException
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.datastore.generated.model.Author
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.storage.StoragePath
import com.amplifyframework.storage.result.StorageUploadFileResult
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

data class PublishUIState (
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


    fun publishBook(onPublishSuccess: () -> Unit) {
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

            val imageKey = selectedImageUri?.let {uri ->
                try {
                    "${UUID.randomUUID()}.jpg".also {key ->
                        uploadImage(key, uri)
                        Log.d(TAG, "Uploaded Image successfully")}
                }
                catch (e: Exception) {
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

                Amplify.API.mutate(ModelMutation.create(book))
                Log.d(TAG, "Successfully created book: ${book.title}")

                BookyoAnalytics.trackApiCall(
                    endpoint = "createBook",
                    isSuccess = true,
                    durationMs = System.currentTimeMillis() - start
                )



            } catch (e: Exception) {
                Log.d(TAG, "Failed to create book")
                BookyoAnalytics.trackApiCall(
                    endpoint = "createBook",
                    isSuccess = false,
                    durationMs = System.currentTimeMillis() - start,
                    errorType = e.javaClass.simpleName,
                    errorMessage = e.message
                )

            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    publishState = PublishState.SUCCESS
                )
            }

        }
    }

    private suspend fun checkAuthorByName(name: String): Author? {
        try {
            val res = Amplify.API.query(
                ModelQuery.list(Author::class.java, Author.NAME.eq(name))
            )

            return res.data.items.firstOrNull()?.let {
                author ->
                Log.d(TAG, "Found author $name")
                author
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failed to check author")
            throw Exception("Failed to check author $name")
        }
    }



    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private suspend fun uploadImage(key: String, uri: Uri): StorageUploadFileResult {
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

            result
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
