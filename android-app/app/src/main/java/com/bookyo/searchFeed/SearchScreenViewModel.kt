package com.bookyo.searchFeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.PaginatedResult
import com.amplifyframework.api.graphql.model.ModelPagination
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.model.LoadedModelReference
import com.amplifyframework.core.model.includes
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.datastore.generated.model.BookPath
import com.amplifyframework.datastore.generated.model.Listing
import com.amplifyframework.datastore.generated.model.ListingPath
import com.amplifyframework.kotlin.core.Amplify
import com.bookyo.analytics.BookyoAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.IOException

class SearchScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SearchScreenUIState>(SearchScreenUIState.Loading)
    val uiState: StateFlow<SearchScreenUIState> = _uiState.asStateFlow()

    // Initial page size for pagination
    private val pageSize = 10

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = SearchScreenUIState.Loading
            try {
                val books = fetchBooksPageOne()
                val listings = fetchListingsPageOne()

                // After fetching data, map them to UI models
                if (books != null && listings != null) {
                    mapBooksToUiModels(books, listings)
                } else if (books != null) {
                    mapBooksToUiModels(books, null)
                } else {
                    _uiState.value = SearchScreenUIState.Empty
                }

            } catch (e: IOException) {
                _uiState.value = SearchScreenUIState.Error.Network(this@SearchScreenViewModel::loadInitialData)
            } catch (e: Exception) {
                _uiState.value = SearchScreenUIState.Error.Generic(
                    message = e.message ?: "An unknown error occurred",
                    retry = this@SearchScreenViewModel::loadInitialData
                )
            }
        }
    }

    private suspend fun fetchBooksPageOne(): List<Book>? = withContext(Dispatchers.IO) {
        try {
            val request = ModelQuery.list<Book, BookPath>(
                Book::class.java,
                ModelPagination.firstPage().withLimit(pageSize)
            ) { bookPath ->
                includes(bookPath.author)
            }

            fetchBooks(request)
        } catch (e: Exception) {
            BookyoAnalytics.trackApiCall(
                endpoint = "fetchBooksPageOne",
                isSuccess = false,
                durationMs = 0L,
                errorType = e.javaClass.simpleName,
                errorMessage = e.message
            )
            null
        }
    }

    private suspend fun fetchBooks(request: GraphQLRequest<PaginatedResult<Book>>): List<Book>? = withContext(Dispatchers.IO) {
        try {
            val response = Amplify.API.query(request)

            // Null safety check for the response data
            if (response.data == null) {
                BookyoAnalytics.trackApiCall(
                    endpoint = "fetchBooks",
                    isSuccess = false,
                    durationMs = 0L,
                    errorType = "NullDataResponse",
                    errorMessage = "API returned null data"
                )
                return@withContext null
            }

            BookyoAnalytics.trackApiCall(
                endpoint = "fetchBooks",
                isSuccess = true,
                durationMs = 0L
            )

            response.data.items.toList()
        } catch (e: Exception) {
            BookyoAnalytics.trackApiCall(
                endpoint = "fetchBooks",
                isSuccess = false,
                durationMs = 0L,
                errorType = e.javaClass.simpleName,
                errorMessage = e.message
            )
            null
        }
    }


    private suspend fun fetchListingsPageOne(): List<Listing>? = withContext(Dispatchers.IO) {
        try {
            val request = ModelQuery.list<Listing, ListingPath>(
                Listing::class.java,
                ModelPagination.firstPage().withLimit(pageSize)
            ) { listingPath ->
                includes(listingPath.user, listingPath.book)
            }

            fetchListings(request)
        } catch (e: Exception) {
            BookyoAnalytics.trackApiCall(
                endpoint = "fetchListingsPageOne",
                isSuccess = false,
                durationMs = 0L,
                errorType = e.javaClass.simpleName,
                errorMessage = e.message
            )
            null
        }
    }


    private suspend fun fetchListings(request: GraphQLRequest<PaginatedResult<Listing>>): List<Listing>? = withContext(Dispatchers.IO) {
        try {
            val response = Amplify.API.query(request)

            if (response.data == null) {
                BookyoAnalytics.trackApiCall(
                    endpoint = "fetchListings",
                    isSuccess = false,
                    durationMs = 0L,
                    errorType = "NullDataResponse",
                    errorMessage = "API returned null data"
                )
                return@withContext null
            }

            BookyoAnalytics.trackApiCall(
                endpoint = "fetchListings",
                isSuccess = true,
                durationMs = 0L
            )

            response.data.items.toList()
        } catch (e: Exception) {
            BookyoAnalytics.trackApiCall(
                endpoint = "fetchListings",
                isSuccess = false,
                durationMs = 0L,
                errorType = e.javaClass.simpleName,
                errorMessage = e.message
            )
            null
        }
    }


    private fun mapBooksToUiModels(books: List<Book>?, listings: List<Listing>?) {
        // Check if we have valid book data
        if (books.isNullOrEmpty()) {
            _uiState.value = SearchScreenUIState.Empty
            return
        }

        // Create a map of book IDs to their listings
        val bookIdToListings = listings?.groupBy { listing ->
            try {
                val loadedBook = (listing.book as? LoadedModelReference)?.value
                loadedBook?.id
            } catch (e: Exception) {
                null
            }
        }?.filterKeys { it != null } ?: emptyMap()

        // Map books to UI models
        val bookUIModels = books.mapNotNull { book ->
            try {
                book.author?.let { author ->
                    val loadedAuthor = (author as? LoadedModelReference)?.value
                    if (loadedAuthor != null) {
                        // Check if this book has any listings
                        val hasListings = bookIdToListings[book.id]?.isNotEmpty() == true

                        BookUIModel(
                            id = book.id,
                            title = book.title,
                            author = AuthorUIModel(
                                id = loadedAuthor.id,
                                name = loadedAuthor.name
                            ),
                            isbn = book.isbn,
                            thumbnail = book.thumbnail,
                            isListed = hasListings
                        )
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }

        // Map listings to UI models
        val listingUIModels = listings?.mapNotNull { listing ->
            try {
                val loadedUser = listing.user?.let { user ->
                    (user as? LoadedModelReference)?.value
                }
                val loadedBook = listing.book?.let { book ->
                    (book as? LoadedModelReference)?.value
                }

                if (loadedUser != null && loadedBook != null) {
                    ListingUIModel(
                        id = loadedBook.id, // Use the book ID to match with BookUIModel
                        seller = loadedUser.email,
                        price = listing.price
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }

        // Update UI state
        if (bookUIModels.isEmpty()) {
            _uiState.value = SearchScreenUIState.Empty
        } else {
            _uiState.value = SearchScreenUIState.Success(
                books = bookUIModels,
                isLoadingMore = false,
                canLoadMore = bookUIModels.size >= pageSize,
                listings = listingUIModels
            )
        }
    }
}