package com.duke.elliot.kim.kotlin.photodiary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import kotlinx.coroutines.*

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    var diariesFragmentAction = Action.UNINITIALIZED
    var photosFragmentAction = Action.UNINITIALIZED
    private lateinit var database: DiaryDao

    init {
        scope.launch {
            withContext(Dispatchers.IO) {
                database = DiaryDatabase.getInstance(application).dao()
            }
        }
    }

    val diaries = MutableLiveData<ArrayList<DiaryModel>>()

    fun insert(diary: DiaryModel) {
        scope.launch {
            withContext(Dispatchers.IO) {
                database.insert(diary)
            }
        }
    }

    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }
}