package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities

class DiaryViewPagerViewModelFactory(private val database: DiaryDao, private val fileUtilities: FileUtilities): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(DiaryViewPagerViewModel::class.java)) {
            return DiaryViewPagerViewModel(database, fileUtilities) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}