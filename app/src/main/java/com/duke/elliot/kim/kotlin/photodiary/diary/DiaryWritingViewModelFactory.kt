package com.duke.elliot.kim.kotlin.photodiary.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DiaryWritingViewModelFactory(private val diary: DiaryModel?): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(DiaryWritingViewModel::class.java)) {
            return DiaryWritingViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}