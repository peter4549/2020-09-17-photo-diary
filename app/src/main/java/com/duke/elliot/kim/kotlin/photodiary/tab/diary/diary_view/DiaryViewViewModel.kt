package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel

class DiaryViewViewModel: ViewModel() {
    private lateinit var diary: DiaryModel
    var initialized = false

    fun setDiary(diary: DiaryModel) {
        this.diary = diary
    }

    fun getDiary() = this.diary

    fun getMediaList() = diary.mediaArray.toList()
}