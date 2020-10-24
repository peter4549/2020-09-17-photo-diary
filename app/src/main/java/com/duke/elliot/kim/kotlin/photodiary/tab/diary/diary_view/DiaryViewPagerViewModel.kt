package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import kotlinx.coroutines.*
import timber.log.Timber

class DiaryViewPagerViewModel(val database: DiaryDao,  private val fileUtilities: FileUtilities): ViewModel() {
    lateinit var initialDiary: DiaryModel
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    var initialized = false
    var status = -1

    var diaries = database.getAll()

    fun getInitialDiaryPosition() = diaries.value?.indexOf(initialDiary) ?: -1

    fun getItem(position: Int) = diaries.value?.get(position)

    fun update(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                status = UPDATED
                database.update(diary)
            }
        }
    }

    fun delete(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                status = DELETED
                database.delete(diary)

                for (media in diary.mediaArray) {
                    val path = fileUtilities.getPath(media.uriString.toUri())
                    path?.let { MediaHelper.deleteFile(it) } ?: run {
                        Timber.e("File path not found.")
                    }
                }
            }
        }
    }

    companion object {
        const val DELETED = 0
        const val UPDATED = 1
    }
}