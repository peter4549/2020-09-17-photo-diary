package com.duke.elliot.kim.kotlin.photodiary

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.folder.DEFAULT_FOLDER_ID
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel
import com.duke.elliot.kim.kotlin.photodiary.google_map.PlaceModel
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import kotlinx.coroutines.*
import timber.log.Timber

const val SHOW_FAVORITES = -2213L
// DEFAULT_FOLDER_ID is -1L.
// HASHTAG_SELECTED is -825L.

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val fileUtilities = FileUtilities.getInstance(application)
    var photosFragmentAction = Action.UNINITIALIZED
    var database: DiaryDao = DiaryDatabase.getInstance(application).diaryDao()
    private var diaries: LiveData<MutableList<DiaryModel>> = database.getAll()

    lateinit var folderDao: FolderDao
    lateinit var folders: LiveData<MutableList<FolderModel>>

    lateinit var selectedHashTag: String
    var selectedPlace: PlaceModel? = null

    var selectedFolderId = MutableLiveData<Long>().apply {
        value = DEFAULT_FOLDER_ID
    }

    init {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                // database = DiaryDatabase.getInstance(application).diaryDao()
                folderDao = DiaryDatabase.getInstance(application).folderDao()
                // diaries = database.getAll()
                folders = folderDao.getAll()
            }
        }
    }

    fun getDiaries(): LiveData<MutableList<DiaryModel>>? {
        return diaries
    }

    fun getDiaryFolders() =
        if (::folders.isInitialized)
            folders
        else
            null

    fun insert(diary: DiaryModel, folder: FolderModel?) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                inserted = true
                val diaryId = database.insert(diary)

                folder?.let {
                    val diaryIds = it.diaryIds.toMutableList()
                    if (!diaryIds.contains(diaryId)) {
                        diaryIds.add(diaryId)
                        it.diaryIds = diaryIds.toTypedArray()
                        folderDao.update(it)
                    }
                }
            }
        }
    }

    fun update(diary: DiaryModel, folder: FolderModel?) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                updated = true
                database.update(diary)

                folder?.let {
                    insertDiaryToFolder(diary.id, it)
                }
            }
        }
    }

    fun delete(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                folderDao.getFolderById(diary.folderId)?.let {
                    removeDiaryFromFolder(diary.id, it)
                } ?: run {
                    Timber.d("Folder not found.")
                }

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

    fun removeDiaryFromFolder(diaryId: Long, folder: FolderModel) {
        val diaryIds = folder.diaryIds.toMutableList()
        diaryIds.remove(diaryId)
        folder.diaryIds = diaryIds.toTypedArray()
        folderDao.update(folder)
    }

    private fun insertDiaryToFolder(diaryId: Long, folder: FolderModel) {
        val diaryIds = folder.diaryIds.toMutableList()

        if (!diaryIds.contains(diaryId)) {
            diaryIds.add(diaryId)
            folder.diaryIds = diaryIds.toTypedArray()
            folderDao.update(folder)
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