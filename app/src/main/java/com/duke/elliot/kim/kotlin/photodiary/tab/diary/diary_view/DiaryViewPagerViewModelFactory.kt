package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import java.time.LocalDate

class DiaryViewPagerViewModelFactory(private val database: DiaryDao, private val fileUtilities: FileUtilities, private val selectedDate: LocalDate?): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(DiaryViewPagerViewModel::class.java)) {
            return DiaryViewPagerViewModel(database, fileUtilities, selectedDate) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}