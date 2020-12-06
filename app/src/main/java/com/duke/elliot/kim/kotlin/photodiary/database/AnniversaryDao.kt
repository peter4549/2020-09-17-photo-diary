package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.duke.elliot.kim.kotlin.photodiary.calendar.AnniversaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel

@Dao
interface AnniversaryDao {
    @Query("SELECT * FROM anniversary")
    fun getAll(): LiveData<MutableList<AnniversaryModel>>
    @Insert
    fun insert(anniversary: AnniversaryModel)

    @Delete
    fun delete(anniversary: AnniversaryModel)

    @Query("DELETE FROM anniversary")
    fun nukeTable()

    @Update
    fun update(anniversary: AnniversaryModel)
}