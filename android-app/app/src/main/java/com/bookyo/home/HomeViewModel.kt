package com.bookyo.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelPagination
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.kotlin.core.Amplify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class HomeViewModel : ViewModel() {

    //Usar state flows
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

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