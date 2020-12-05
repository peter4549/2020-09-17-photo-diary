package com.duke.elliot.kim.kotlin.photodiary.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup.BACKUP_DIR
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup.BackupUtil
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup.DATA_BACKUP_FILE_NAME
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class DatabaseOpenHelper(val context: Context, databaseName: String): SQLiteOpenHelper(DatabaseContext(context), databaseName, null, 1) {
    override fun onCreate(database: SQLiteDatabase?) {  }

    override fun onUpgrade(database: SQLiteDatabase?, p1: Int, p2: Int) {  }

    @Throws(IOException::class)
    fun importDatabase(context: Context): Boolean {
        close()

        val databaseFile = File(context.getDatabasePath(DIARY_DATABASE_NAME).absolutePath)// File("/data/data/${context.packageName}/databases/$DIARY_DATABASE_NAME.db")
        val backupDir = File(Environment.getExternalStorageDirectory(), BACKUP_DIR)
        val fileName = DIARY_DATABASE_NAME
        val saveFilePath = backupDir.path + File.separator.toString() + fileName


        val newDb = File(saveFilePath)
        // val oldDb = File(databaseFile)
        if (newDb.exists()) {
            println("backupDir ${saveFilePath}")
            println("databaseFile ${databaseFile.absolutePath}")
            println("databaseFile2 ${context.getDatabasePath(DIARY_DATABASE_NAME).absolutePath}")
            BackupUtil.copyFile(FileInputStream(newDb), FileOutputStream(databaseFile))
            // Access the copied database so SQLiteHelper will cache it and mark
            // it as created.
            // writableDatabase.close()


            return true
        }

        return false
    }
}