package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import kotlinx.coroutines.*
import timber.log.Timber
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId

class DiaryViewPagerViewModel(val database: DiaryDao,  private val fileUtilities: FileUtilities,
                              val selectedDate: LocalDate?): ViewModel() {
    lateinit var currentDiary: DiaryModel
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    var deletedDiaryPosition = -1
    var initialized = false
    var status = DEFAULT
    var tomorrow = selectedDate?.plusDays(1)

    var diaries =
        if (selectedDate == null)
            database.getAll()
        else
            database.getSelectedDateDiaries(selectedDate.toLong(), tomorrow?.toLong() ?: -1)

    fun getCurrentDiaryPosition() = diaries.value?.indexOf(currentDiary) ?: -1

    fun getItem(position: Int) = diaries.value?.get(position)

    /*
    fun update(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                status = UPDATED
                database.update(diary)
            }
        }
    }
     */

    fun delete(diary: DiaryModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                deletedDiaryPosition = diaries.value?.indexOf(diary) ?: -1
                database.delete(diary)
                status = DELETED

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
        const val DEFAULT = 0
        const val DELETED = 1
    }
}

fun LocalDate.toLong(): Long {
    val defaultZoneId = ZoneId.systemDefault()
    val date = Date.from(this.atStartOfDay(defaultZoneId).toInstant())

    return date.time
}