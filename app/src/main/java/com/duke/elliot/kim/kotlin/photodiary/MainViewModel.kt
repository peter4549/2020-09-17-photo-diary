package com.duke.elliot.kim.kotlin.photodiary

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen.LockScreenHelper
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel
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
    private lateinit var database: DiaryDao
    private lateinit var diaries: LiveData<MutableList<DiaryModel>>

    private lateinit var folderDao: FolderDao
    lateinit var folders: LiveData<MutableList<FolderModel>>

    init {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                database = DiaryDatabase.getInstance(application).diaryDao()
                folderDao = DiaryDatabase.getInstance(application).folderDao()
                diaries = database.getAll()
                folders = folderDao.getAll()
            }
        }
    }

    fun getDiaries(): LiveData<MutableList<DiaryModel>>? {
        return if (::diaries.isInitialized)
            diaries
        else
            null
    }

    fun getDiaryFolders() =
        if (::folders.isInitialized)
            folders
        else
            null

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

        var screenWasOff = true
        var lockScreenException = false
    }
}