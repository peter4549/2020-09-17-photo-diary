package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.app.Application
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BackupViewModel(application: Application): ViewModel() {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private var fileUtil: FileUtilities = FileUtilities.getInstance(application)
    val diaryDao = DiaryDatabase.getInstance(application).diaryDao()
    lateinit var diaries: List<DiaryModel>

    init {
        coroutineScope.launch {
            diaries = diaryDao.getAllValues()
        }
    }

    fun deleteFiles(paths: List<String>) {
        coroutineScope.launch {
            fileUtil.deleteFiles(paths)
        }
    }

    fun getAllMedia(): List<MediaModel> {
        val mediaList = mutableListOf<MediaModel>()

        for (diary in diaries) {
            mediaList += diary.mediaArray
        }

        return mediaList
    }
}