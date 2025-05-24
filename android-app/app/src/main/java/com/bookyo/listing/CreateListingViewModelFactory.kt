
package com.bookyo.listing

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class CreateListingViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateListingViewModel::class.java)) {
            return CreateListingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}