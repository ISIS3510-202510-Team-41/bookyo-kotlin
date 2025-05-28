package com.bookyo.bookDetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.model.LoadedModelReference
import com.amplifyframework.core.model.includes
import com.amplifyframework.datastore.generated.model.Author
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.datastore.generated.model.BookPath
import com.amplifyframework.datastore.generated.model.BookWishlist
import com.amplifyframework.datastore.generated.model.Listing
import com.amplifyframework.datastore.generated.model.ListingPath
import com.amplifyframework.datastore.generated.model.Wishlist
import com.amplifyframework.kotlin.core.Amplify
import com.bookyo.analytics.BookyoAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

data class BookDetailUIState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val authorName: String = "",
    val errorMessage: String? = null,
    val hasListing: Boolean = false,
    val listingPrice: String = "",
    val isInWishlist: Boolean = false
)

class BookDetailViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "BookDetailViewModel"
    }

    private val _uiState = MutableStateFlow(BookDetailUIState())
    val uiState: StateFlow<BookDetailUIState> = _uiState.asStateFlow()

    private var bookId: String? = null
    private var currentUserId: String? = null
    private var wishlistId: String? = null

    init {
        viewModelScope.launch {
            try {
                // Get current user
                val authUser = Amplify.Auth.getCurrentUser()
                currentUserId = authUser.userId

                // Load user's wishlist ID
                loadWishlistId()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current user", e)
            }
        }
    }

    private suspend fun loadWishlistId() {
        try {
            val userId = currentUserId ?: return

            val response = withContext(Dispatchers.IO) {
                Amplify.API.query(
                    ModelQuery.list(
                        Wishlist::class.java,
                        Wishlist.ID.eq(userId)
                    )
                )
            }

            wishlistId = response.data.items.firstOrNull()?.id
            Log.d(TAG, "Loaded wishlist ID: $wishlistId")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading wishlist", e)
        }
    }

    fun loadBookDetails(bookId: String) {
        this.bookId = bookId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Variables para mantener resultados de IO
                val book: Book?
                var authorName = ""
                var hasListing = false
                var listingPrice = ""
                var isInWishlist = false

                withContext(Dispatchers.IO) {
                    book = fetchBook(bookId)

                    if (book != null) {
                        authorName = (book.author as? LoadedModelReference<Author>)?.value?.name ?: ""

                        val listings = fetchListings(bookId)
                        hasListing = !listings.isNullOrEmpty()
                        listingPrice = if (hasListing) {
                            String.format("$%.2f", listings.first().price)
                        } else ""

                        isInWishlist = checkWishlist(bookId)
                    }
                }

                // Actualización de la UI en el hilo principal
                if (book != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            book = book,
                            authorName = authorName,
                            hasListing = hasListing,
                            listingPrice = listingPrice,
                            isInWishlist = isInWishlist
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Book not found"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading book details", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load book details"
                    )
                }
            }
        }
    }


    private suspend fun fetchBook(bookId: String): Book? = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()

        try {
            val request = ModelQuery.get<Book, BookPath>(
                Book::class.java,
                bookId
            ) { bookPath ->
                includes(bookPath.author)
            }

            val response = Amplify.API.query(request)

            if (response.hasErrors()) {
                throw Exception(response.errors.first().message)
            }

            BookyoAnalytics.trackApiCall(
                "fetchBook",
                true,
                System.currentTimeMillis() - start,
                null,
                null,
                null
            )

            response.data
        } catch (e: Exception) {
            BookyoAnalytics.trackApiCall(
                "fetchBook",
                false,
                System.currentTimeMillis() - start,
                e.javaClass.simpleName,
                e.message,
                null
            )

            Log.e(TAG, "Error fetching book", e)
            null
        }
    }


    private suspend fun fetchListings(bookId: String): List<Listing>? = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()

        try {
            val request = ModelQuery.list<Listing, ListingPath>(
                Listing::class.java,
                Listing.BOOK.eq(bookId)
            ) { listingPath ->
                includes(listingPath.user, listingPath.book)
            }

            val response = Amplify.API.query(request)

            if (response.hasErrors()) {
                throw Exception(response.errors.first().message)
            }

            BookyoAnalytics.trackApiCall(
                "fetchListings",
                true,
                System.currentTimeMillis() - start,
                null,
                null,
                null
            )

            response.data.items.toList()
        } catch (e: Exception) {
            BookyoAnalytics.trackApiCall(
                "fetchListings",
                false,
                System.currentTimeMillis() - start,
                e.javaClass.simpleName,
                e.message,
                null
            )

            Log.e(TAG, "Error fetching listings", e)
            null
        }
    }


    private suspend fun checkWishlist(bookId: String): Boolean = withContext(Dispatchers.IO) {
        val wishlistId = this@BookDetailViewModel.wishlistId ?: return@withContext false

        try {
            val response = Amplify.API.query(
                ModelQuery.list(
                    BookWishlist::class.java,
                    BookWishlist.ID.eq(bookId).and(BookWishlist.ID.eq(wishlistId))
                )
            )

            val res = response.data.items
            res.iterator().hasNext()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking wishlist", e)
            false
        }
    }


    fun addToWishlist() {
        viewModelScope.launch {
            try {
                val bookId = this@BookDetailViewModel.bookId ?: return@launch
                val wishlistId = this@BookDetailViewModel.wishlistId ?: return@launch

                // Create a new BookWishlist entry
                val bookWishlist = BookWishlist.builder()
                    .id(bookId)
                    .id(wishlistId)
                    .build()

                Amplify.API.mutate(
                    ModelMutation.create(bookWishlist)
                )

                _uiState.update { it.copy(isInWishlist = true) }

            } catch (e: Exception) {
                Log.e(TAG, "Error adding to wishlist", e)
                _uiState.update { it.copy(errorMessage = "Failed to add to wishlist") }
            }
        }
    }

    fun removeFromWishlist() {
        viewModelScope.launch {
            try {
                val bookId = this@BookDetailViewModel.bookId ?: return@launch
                val wishlistId = this@BookDetailViewModel.wishlistId ?: return@launch

                // Find the BookWishlist entry
                val response = Amplify.API.query(
                    ModelQuery.list(
                        BookWishlist::class.java,
                        BookWishlist.ID.eq(bookId).and(BookWishlist.ID.eq(wishlistId))
                    )
                )

                val bookWishlist = response.data.items.firstOrNull() ?: return@launch


                Amplify.API.mutate(
                    ModelMutation.delete(bookWishlist)
                )

                _uiState.update { it.copy(isInWishlist = false) }

            } catch (e: Exception) {
                Log.e(TAG, "Error removing from wishlist", e)
                _uiState.update { it.copy(errorMessage = "Failed to remove from wishlist") }
            }
        }
    }

    fun retryLoadingBook() {
        bookId?.let { loadBookDetails(it) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}