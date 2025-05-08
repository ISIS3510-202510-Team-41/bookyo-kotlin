package com.bookyo.searchFeed

import androidx.compose.runtime.Immutable

@Immutable
data class BookUIModel(
    val id: String,
    val title: String,
    val author: AuthorUIModel,
    val isbn: String,
    val thumbnail: String?,
    val isListed: Boolean = false
)

@Immutable
data class ListingUIModel(
    val id: String,
    val seller: String,
    val price: Double
)

@Immutable
data class AuthorUIModel(
    val id: String, val name: String
)

sealed class SearchScreenUIState {
    data object Loading : SearchScreenUIState()
    data class Success(
        val books: List<BookUIModel>,
        val listings: List<ListingUIModel>?,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = true
    ) : SearchScreenUIState()
    sealed class Error : SearchScreenUIState() {
        data class Network(val retry: () -> Unit) : Error()
        data class Generic(val message: String, val retry: () -> Unit) : Error()
    }

    data object Empty : SearchScreenUIState()
}