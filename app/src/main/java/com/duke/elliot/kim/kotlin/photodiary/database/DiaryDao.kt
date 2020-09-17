package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diarymodel")
    fun getAll(): LiveData<MutableList<DiaryModel>>

    @Insert
    fun insert(note: DiaryModel)

    @Delete
    fun delete(note: DiaryModel)

    @Query("DELETE FROM diarymodel")
    fun nukeTable()
}