package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BackupViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(BackupViewModel::class.java)) {
            return BackupViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}