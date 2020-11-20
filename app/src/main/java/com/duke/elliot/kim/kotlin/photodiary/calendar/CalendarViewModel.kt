package com.duke.elliot.kim.kotlin.photodiary.calendar

import android.app.Application
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import kotlinx.coroutines.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class CalendarViewModel(application: Application): ViewModel() {
    private val anniversaryDao = DiaryDatabase.getInstance(application).anniversaryDao()
    private val diaryDao = DiaryDatabase.getInstance(application).diaryDao()
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    val anniversaryList = anniversaryDao.getAll()
    val dateDiaryHashMap: HashMap<LocalDate, MutableList<DiaryModel>> = hashMapOf()
    val diaries = diaryDao.getAll()
    var status = UNINITIALIZED
    var hashMapStatus = HASH_MAP_UNINITIALIZED

    fun getAnniversary(date: LocalDate): AnniversaryModel? {
        for (anniversary in anniversaryList.value ?: return null) {

            if (anniversary.month == date.monthValue && anniversary.day == date.dayOfMonth) {
                if (anniversary.annual)
                    return anniversary
                else if (anniversary.year == date.year)
                    return anniversary
            }
        }

        return null
    }

    fun registerAnniversary(anniversaryModel: AnniversaryModel) {
        coroutineScope.launch {
            anniversaryDao.insert(anniversaryModel)
        }
    }

    fun unregisterAnniversary(anniversaryModel: AnniversaryModel) {
        status = DELETED
        coroutineScope.launch {
            anniversaryDao.delete(anniversaryModel)
        }
    }

    fun updateAnniversary(anniversaryModel: AnniversaryModel) {
        coroutineScope.launch {
            anniversaryDao.update(anniversaryModel)
        }
    }

    fun createDateDiaryHashMap(diaries: List<DiaryModel>, completeListener: ((LocalDate) -> Unit)?) {
        coroutineScope.launch {
            for (diary in diaries) {
                val localDate = Instant.ofEpochMilli(diary.time)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                dateDiaryHashMap[localDate]?.add(diary) ?: run {
                    dateDiaryHashMap.put(localDate, mutableListOf(diary))
                }
            }

            withContext(Dispatchers.Main) {
                for (date in dateDiaryHashMap.keys)
                    completeListener?.invoke(date)
            }
        }
    }

    fun putDiaryToDataDiaryHashMap(diary: DiaryModel) {
        val localDate = Instant.ofEpochMilli(diary.time)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        dateDiaryHashMap[localDate]?.add(diary) ?: run {
            dateDiaryHashMap.put(localDate, mutableListOf(diary))
        }
    }

    companion object {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val DELETED = 2

        const val HASH_MAP_UNINITIALIZED = 0
        const val HASH_MAP_INITIALIZED = 1
    }
}