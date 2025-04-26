package com.bookyo.searchFeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelPagination
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.datastore.generated.model.Listing
import com.amplifyframework.kotlin.core.Amplify
import com.bookyo.analytics.BookyoAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class SearchScreenViewModel : ViewModel() {

    //Usar state flows
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()
    val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    init {
        loadBooks()
        loadListings()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            val books = fetchBooks()
            _books.value = books
        }
    }

    private fun loadListings() {
        viewModelScope.launch {
            val listings = fetchListings()
            _listings.value = listings
        }
    }

    private suspend fun fetchBooks(): List<Book> = supervisorScope {

        try {
            val limit = 10
            val response = Amplify.API.query(
                ModelQuery.list(
                    Book::class.java,
                    ModelPagination.limit(limit))
            )
            response.data.items.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchListings(): List<Listing> = supervisorScope {
        try {
            val limit = 10
            val response = Amplify.API.query(
                ModelQuery.list(
                    Listing::class.java,
                    ModelPagination.limit(limit))
            )
            response.data.items.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}