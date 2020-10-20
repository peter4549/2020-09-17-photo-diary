package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DiaryWritingViewModelFactory(private val application: Application, private val diary: DiaryModel?): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(DiaryWritingViewModel::class.java)) {
            return DiaryWritingViewModel(application, diary) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}