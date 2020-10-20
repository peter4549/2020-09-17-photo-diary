package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel

class DiaryViewPagerViewModel(val database: DiaryDao): ViewModel() {
    lateinit var initialDiary: DiaryModel
    var initialized = false

    var diaries = database.getAll()

    fun getInitialDiaryPosition() = diaries.value?.indexOf(initialDiary) ?: -1

    fun getItem(position: Int) = diaries.value?.get(position)
}