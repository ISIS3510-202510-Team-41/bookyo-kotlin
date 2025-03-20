package com.bookyo.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelPagination
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.kotlin.core.Amplify
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class HomeViewModel : ViewModel() {

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> get() = _books

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            val books = fetchBooks()
            _books.value = books
        }
    }

    private suspend fun fetchBooks(): List<Book> = supervisorScope {
        try {
            val limit = 5
            val response = Amplify.API.query(
                ModelQuery.list(Book::class.java,
                    ModelPagination.limit(limit))
            )

            response.data.items.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}