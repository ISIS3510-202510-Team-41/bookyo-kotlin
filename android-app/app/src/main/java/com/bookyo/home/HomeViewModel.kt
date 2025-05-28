package com.bookyo.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.model.ModelPagination
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.datastore.generated.model.Book
import com.amplifyframework.kotlin.core.Amplify
import com.bookyo.analytics.BookyoAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class HomeViewModel() : ViewModel() {

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

    private suspend fun fetchBooks(): List<Book> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val limit = 5
            val response = Amplify.API.query(
                ModelQuery.list(Book::class.java, ModelPagination.limit(limit))
            )
            val duration = System.currentTimeMillis() - start
            BookyoAnalytics.trackApiCall("loadBooks", true, duration, null, null, null)
            response.data.items.toList()
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - start
            BookyoAnalytics.trackApiCall("loadBooks", false, duration, e.javaClass.simpleName, e.message, null)
            emptyList()
        }
    }
}