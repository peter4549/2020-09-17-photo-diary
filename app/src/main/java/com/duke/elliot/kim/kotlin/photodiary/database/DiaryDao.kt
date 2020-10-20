package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel

@Dao
interface DiaryDao {
    // ORDER BY id DESC
    @Query("SELECT * FROM diary ORDER BY id DESC")
    fun getAll(): LiveData<MutableList<DiaryModel>>

    @Insert
    fun insert(dairy: DiaryModel)

    @Delete
    fun delete(diary: DiaryModel)

    @Query("DELETE FROM diary")
    fun nukeTable()

    @Update
    fun update(diary: DiaryModel)
}