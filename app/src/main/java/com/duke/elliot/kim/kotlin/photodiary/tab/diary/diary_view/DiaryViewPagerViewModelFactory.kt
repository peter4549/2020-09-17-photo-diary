package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao

class DiaryViewPagerViewModelFactory(private val database: DiaryDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(DiaryViewPagerViewModel::class.java)) {
            return DiaryViewPagerViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}