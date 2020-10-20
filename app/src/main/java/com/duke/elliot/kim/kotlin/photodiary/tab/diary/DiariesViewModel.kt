package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import kotlinx.coroutines.*
import timber.log.Timber

class DiariesViewModel(val database: DiaryDao, application: Application): AndroidViewModel(application) {
    private val fileUtilities = FileUtilities.getInstance(application)
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    val diaries = database.getAll()
    var status = UNINITIALIZED

    fun delete(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
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

    fun update(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                database.update(diary)
            }
        }
    }

    companion object {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
    }
}