package com.duke.elliot.kim.kotlin.photodiary

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.LocalDate

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val fileUtilities = FileUtilities.getInstance(application)
    // var diariesFragmentAction = Action.UNINITIALIZED
    var photosFragmentAction = Action.UNINITIALIZED
    var lockScreenOn = false
    private lateinit var database: DiaryDao
    private lateinit var diaries: LiveData<MutableList<DiaryModel>>

    init {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                database = DiaryDatabase.getInstance(application).diaryDao()
                diaries = database.getAll()
            }
        }
    }

    fun getDiaries(): LiveData<MutableList<DiaryModel>>? {
        return if (::diaries.isInitialized)
            diaries
        else
            null
    }

    fun insert(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                inserted = true
                database.insert(diary)
            }
        }
    }

    fun update(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                updated = true
                database.update(diary)
            }
        }
    }

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

    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }

    companion object {
        var inserted = false
        var updated = false
        var selectedDiaryPosition = -1
    }
}