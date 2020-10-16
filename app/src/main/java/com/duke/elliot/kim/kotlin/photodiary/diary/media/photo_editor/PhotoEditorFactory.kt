package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PhotoEditorFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(PhotoEditorViewModel::class.java)) {
            return PhotoEditorViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}