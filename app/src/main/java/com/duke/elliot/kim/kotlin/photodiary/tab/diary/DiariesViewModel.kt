package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class DiariesViewModel(val database: DiaryDao, application: Application): AndroidViewModel(application) {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    val diaries = MutableLiveData<ArrayList<DiaryModel>>()

    companion object {
        const val UNINITIALIZED = 0
    }
}