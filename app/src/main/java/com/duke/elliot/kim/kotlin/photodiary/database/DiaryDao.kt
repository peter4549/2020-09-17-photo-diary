package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary")
    fun getAll(): LiveData<MutableList<DiaryModel>>

    @Insert
    fun insert(dairy: DiaryModel)

    @Delete
    fun delete(diary: DiaryModel)

    @Query("DELETE FROM diary")
    fun nukeTable()
}