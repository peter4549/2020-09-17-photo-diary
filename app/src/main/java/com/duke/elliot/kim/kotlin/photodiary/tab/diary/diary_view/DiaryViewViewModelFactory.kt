package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel

class DiaryViewViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(DiaryViewViewModel::class.java)) {
            return DiaryViewViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}