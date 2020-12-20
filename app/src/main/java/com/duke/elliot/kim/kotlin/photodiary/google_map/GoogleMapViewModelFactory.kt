package com.duke.elliot.kim.kotlin.photodiary.google_map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GoogleMapViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(GoogleMapViewModel::class.java)) {
            return GoogleMapViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}