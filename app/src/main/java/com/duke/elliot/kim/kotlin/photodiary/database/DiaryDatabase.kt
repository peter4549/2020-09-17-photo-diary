package com.duke.elliot.kim.kotlin.photodiary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.duke.elliot.kim.kotlin.photodiary.calendar.AnniversaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel

const val DIARY_DATABASE_NAME = "diary_database_debug_23"

@Database(entities = [DiaryModel::class, AnniversaryModel::class, FolderModel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun anniversaryDao(): AnniversaryDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun releaseInstance() {
            if (INSTANCE?.isOpen == true)
                INSTANCE?.close()
            INSTANCE = null
        }

        fun getInstance(context: Context): DiaryDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DiaryDatabase::class.java,
                        DIARY_DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}