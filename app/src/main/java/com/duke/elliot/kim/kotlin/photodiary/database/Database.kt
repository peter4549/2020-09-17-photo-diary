package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel

@Database(entities = [DiaryModel::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun dao(): DiaryDao
}