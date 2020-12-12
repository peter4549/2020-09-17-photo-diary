package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder ORDER BY name DESC")
    fun getAll(): LiveData<MutableList<FolderModel>>

    @Insert
    fun insert(folder: FolderModel)

    @Delete
    fun delete(folder: FolderModel)

    @Update
    fun update(folder: FolderModel)
}