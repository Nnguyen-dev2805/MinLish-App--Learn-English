package com.example.minlishapp_learnenglish.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
fun <VM : ViewModel> viewModelFactory(create: () -> VM): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        // WordViewModel(repository) as WordViewModel
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return create() as T // không ép được thì crash lúc runtime
        }
    }
}
