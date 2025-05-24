package com.bookyo.listing

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.model.LoadedModelReference
import com.amplifyframework.datastore.generated.model.Author
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.datastore.generated.model.Listing
import com.amplifyframework.datastore.generated.model.ListingStatus
import com.amplifyframework.datastore.generated.model.Notification
import com.amplifyframework.datastore.generated.model.NotificationType
import com.amplifyframework.datastore.generated.model.User
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.storage.StoragePath
import com.bookyo.analytics.BookyoAnalytics
import com.bookyo.utils.ConnectivityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class CreateListingUIState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val bookId: String = "",
    val bookTitle: String = "",
    val bookIsbn: String = "",
    val bookThumbnail: String? = null,
    val authorName: String = "",
    val price: String = "",
    val priceError: String? = null,
    val condition: Int = 3, // Default condition is "Good"
    val description: String = "",
    val images: List<Uri> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val listingCreated: Boolean = false,
    val isConnected: Boolean = true
)

class CreateListingViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "CreateListingViewModel"
    }

    private val _uiState = MutableStateFlow(CreateListingUIState())
    val uiState: StateFlow<CreateListingUIState> = _uiState.asStateFlow()

    // Connectivity checker
    private val connectivityChecker = ConnectivityChecker(application)

    // Repository for pending listings
    private val pendingListingRepository = PendingListingRepository(application)

    init {
        // Observe connectivity changes
        viewModelScope.launch {
            connectivityChecker.observeConnectivity().collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }

                // Check if we just got back online and have pending listings
                if (isConnected) {
                    checkPendingListings()
                }
            }
        }

        // Get current user
        viewModelScope.launch {
            try {
                Amplify.Auth.getCurrentUser()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current user", e)
                _uiState.update { it.copy(
                    errorMessage = "Failed to get user information"
                )}
            }
        }
    }

    /**
     * Check if there are any pending listings to process
     */
    private suspend fun checkPendingListings() {
        val pendingCount = pendingListingRepository.getPendingListings().size
        if (pendingCount > 0) {
            Log.d(TAG, "Connectivity restored with $pendingCount pending listings")
            PendingListingWorker.enqueueWork(getApplication())
        }
    }

    /**
     * Initialize the ViewModel with a book ID
     */
    fun initialize(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                bookId = bookId
            )}

            try {
                // Load book details
                val response = Amplify.API.query(
                    ModelQuery[Book::class.java, bookId]
                )

                if (response.hasErrors()) {
                    throw Exception(response.errors.first().message)
                }

                val book = response.data

                // Extract author info
                val author = (book.author as? LoadedModelReference<Author>)?.value
                val authorName = author?.name ?: "Unknown Author"

                // Update UI state
                _uiState.update { it.copy(
                    isLoading = false,
                    bookTitle = book.title,
                    bookIsbn = book.isbn,
                    bookThumbnail = book.thumbnail,
                    authorName = authorName
                )}

                // Track screen view
                BookyoAnalytics.recordAppEvent(
                    eventName = "screen_view_create_listing",
                    properties = mapOf(
                        "book_id" to bookId,
                        "book_title" to book.title
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error loading book", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load book details: ${e.localizedMessage}"
                )}
            }
        }
    }

    /**
     * Update the price value with validation
     */
    fun updatePrice(newPrice: String) {
        // Only allow numbers and one decimal point
        if (newPrice.isEmpty() || newPrice.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            // Validate price
            val priceError = when {
                newPrice.isEmpty() -> "Price is required"
                newPrice.toDoubleOrNull() == null -> "Invalid price format"
                newPrice.toDouble() <= 0 -> "Price must be greater than zero"
                else -> null
            }

            _uiState.update { it.copy(
                price = newPrice,
                priceError = priceError
            )}
        }
    }

    /**
     * Add an image to the listing
     */
    fun addListingImage(uri: Uri, isFromCamera: Boolean = false) {
        if (_uiState.value.images.size >= 5) return

        val currentImages = _uiState.value.images.toMutableList()
        currentImages.add(uri)

        _uiState.update { it.copy(images = currentImages) }

        BookyoAnalytics.recordAppEvent(
            eventName = if (isFromCamera) "listing_image_captured" else "listing_image_selected",
            properties = mapOf("count" to currentImages.size.toString())
        )
    }

    /**
     * Remove an image from the listing
     */
    fun removeImage(index: Int) {
        if (index < 0 || index >= _uiState.value.images.size) return

        val currentImages = _uiState.value.images.toMutableList()
        currentImages.removeAt(index)

        _uiState.update { it.copy(images = currentImages) }
    }

    /**
     * Create a new listing
     */
    fun createListing() {
        viewModelScope.launch {
            // Validate form
            val validationError = validateForm()
            if (validationError != null) {
                _uiState.update { it.copy(
                    errorMessage = validationError
                )}
                return@launch
            }

            _uiState.update { it.copy(
                isSubmitting = true,
                errorMessage = null,
                successMessage = null
            )}

            // Check connectivity
            if (!connectivityChecker.isConnected()) {
                handleOfflineListing()
                return@launch
            }

            try {
                // Upload listing
                val createListingResult = createListingSync()

                if (createListingResult) {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        successMessage = "Listing created successfully!",
                        listingCreated = true
                    )}

                    BookyoAnalytics.recordAppEvent(
                        eventName = "listing_created",
                        properties = mapOf(
                            "book_id" to _uiState.value.bookId,
                            "price" to _uiState.value.price,
                            "condition" to _uiState.value.condition.toString(),
                            "has_description" to (_uiState.value.description.isNotEmpty()).toString(),
                            "image_count" to _uiState.value.images.size.toString()
                        )
                    )
                } else {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        errorMessage = "Failed to create listing"
                    )}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating listing", e)

                // Check if it's a network error and handle offline
                if (e.message?.contains("UnknownHost") == true ||
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("Network") == true) {
                    handleOfflineListing()
                } else {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        errorMessage = "Error: ${e.message}"
                    )}
                }
            }
        }
    }

    /**
     * Handle creating a listing when offline
     */
    private suspend fun handleOfflineListing() {
        try {
            Log.d(TAG, "No internet connection, saving listing for later")

            // Save the listing request locally
            val pendingListingData = pendingListingRepository.savePendingListing(
                bookId = _uiState.value.bookId,
                price = _uiState.value.price.toDoubleOrNull() ?: 0.0,
                images = _uiState.value.images
            )

            // Enqueue work to process when internet is available
            PendingListingWorker.enqueueSpecificWork(
                getApplication(),
                pendingListingData.id
            )

            // Update UI state
            _uiState.update { it.copy(
                isSubmitting = false,
                successMessage = "Listing saved and will be created when internet is available",
                listingCreated = true
            )}

            BookyoAnalytics.recordAppEvent(
                eventName = "listing_saved_offline",
                properties = mapOf(
                    "book_id" to _uiState.value.bookId,
                    "image_count" to _uiState.value.images.size.toString()
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error saving offline listing", e)
            _uiState.update { it.copy(
                isSubmitting = false,
                errorMessage = "Failed to save offline: ${e.message}"
            )}
        }
    }

    /**
     * Synchronously create a listing
     * This method can be called from the PendingListingWorker
     */
    suspend fun createListingSync(): Boolean {
        return withContext(Dispatchers.IO) {
            // Get values from UI state
            val currentState = _uiState.value
            val bookId = currentState.bookId
            val price = currentState.price.toDoubleOrNull() ?: 0.0
            val images = currentState.images

            if (bookId.isEmpty()) {
                Log.e(TAG, "Missing required data: bookId=$bookId")
                return@withContext false
            }

            val startTime = System.currentTimeMillis()

            try {
                // First get the current user
                val authUserEmail = Amplify.Auth.fetchUserAttributes().first()

                val userEmail = authUserEmail.value

                Log.d(TAG, "Creating listing for book: $bookId, user: $userEmail")

                // Get the Book entity
                val bookResponse = Amplify.API.query(ModelQuery[Book::class.java, bookId])
                if (bookResponse.hasErrors()) {
                    throw Exception("Failed to get book: ${bookResponse.errors.first().message}")
                }
                val book = bookResponse.data
                if (book == null) {
                    throw Exception("Book not found with ID: $bookId")
                }

                // Get the User entity by email (User model uses email as identifier)
                val userResponse = Amplify.API.query(ModelQuery[User::class.java, userEmail])
                if (userResponse.hasErrors()) {
                    throw Exception("Failed to get user: ${userResponse.errors.first().message}")
                }
                val user = userResponse.data
                if (user == null) {
                    throw Exception("User not found with email: $userEmail")
                }

                // Upload images first
                val imageKeys = uploadListingImages(images)
                if (imageKeys.isEmpty()) {
                    throw Exception("Failed to upload images or no images provided")
                }

                Log.d(TAG, "Successfully uploaded ${imageKeys.size} images")

                // Create the listing using the correct builder pattern
                val listing = Listing.builder()
                    .price(price)
                    .photos(imageKeys)
                    .book(book)
                    .user(user)
                    .status(ListingStatus.available)
                    .build()

                Log.d(TAG, "Attempting to create listing with price: $price, photos: ${imageKeys.size}")

                val result = Amplify.API.mutate(ModelMutation.create(listing))

                if (result.hasErrors()) {
                    val errorMessage = result.errors.joinToString(", ") { it.message }
                    throw Exception("GraphQL errors: $errorMessage")
                }

                Log.d(TAG, "Successfully created listing for book $bookId")

                // Create notification about the new listing
                createListingNotification(bookId, price)

                // Track API call success
                BookyoAnalytics.trackApiCall(
                    endpoint = "createListing",
                    isSuccess = true,
                    durationMs = System.currentTimeMillis() - startTime
                )

                return@withContext true
            } catch (e: Exception) {
                Log.e(TAG, "Error creating listing", e)

                // Track API call failure
                BookyoAnalytics.trackApiCall(
                    endpoint = "createListing",
                    isSuccess = false,
                    durationMs = System.currentTimeMillis() - startTime,
                    errorType = e.javaClass.simpleName,
                    errorMessage = e.message
                )

                throw e
            }
        }
    }

    /**
     * Upload listing images to S3 storage
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private suspend fun uploadListingImages(images: List<Uri>): List<String> {
        val imageKeys = mutableListOf<String>()

        for (uri in images) {
            try {
                val contentResolver = getApplication<Application>().contentResolver
                val key = "listing-${UUID.randomUUID()}.jpg"

                val tempFile = File.createTempFile("upload", ".jpg").apply {
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

                upload.result()
                tempFile.delete()

                imageKeys.add(key)
                Log.d(TAG, "Successfully uploaded listing image: $key")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload image", e)
                throw e
            }
        }

        return imageKeys
    }

    /**
     * Create a notification for the new listing
     */
    private suspend fun createListingNotification(bookId: String, price: Double) {
        try {
            // Get book details for the notification
            val bookResponse = Amplify.API.query(ModelQuery[Book::class.java, bookId])

            if (bookResponse.hasErrors()) {
                Log.e(TAG, "Error fetching book for notification: ${bookResponse.errors.first().message}")
                return
            }

            val book = bookResponse.data
            val title = book.title

            // Create a broadcast notification about the new listing
            val notification = Notification.builder()
                .title("New Book Listing")
                .body("\"$title\" is now available for $${String.format("%.2f", price)}!")
                .recipient("*") // Broadcast to all users
                .read(false)
                .type(NotificationType.NEW_BOOK) // Reusing the existing type
                .build()

            val createNotificationResult = Amplify.API.mutate(ModelMutation.create(notification))
            if (createNotificationResult.hasErrors()) {
                Log.e(TAG, "Error creating notification: ${createNotificationResult.errors.first().message}")
            } else {
                Log.d(TAG, "Created notification about new listing")
            }
        } catch (e: Exception) {
            // Just log the error - don't fail the listing creation
            Log.e(TAG, "Failed to create listing notification", e)
        }
    }

    /**
     * Validate the listing form
     */
    private fun validateForm(): String? {
        return when {
            _uiState.value.price.isEmpty() -> "Price is required"
            _uiState.value.price.toDoubleOrNull() == null -> "Invalid price format"
            _uiState.value.price.toDouble() <= 0 -> "Price must be greater than zero"
            _uiState.value.images.isEmpty() -> "At least one image is required"
            else -> null
        }
    }

    /**
     * Clear error and success messages
     */
    fun clearMessages() {
        _uiState.update { it.copy(
            errorMessage = null,
            successMessage = null
        )}
    }
}