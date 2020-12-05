package com.duke.elliot.kim.kotlin.photodiary.database

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.os.Environment
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup.BACKUP_DIR
import java.io.File

internal class DatabaseContext(base: Context?) : ContextWrapper(base) {
    override fun getDatabasePath(name: String): File {
        val sdcard = Environment.getExternalStorageDirectory()
        var dbfile: String =
            sdcard.getAbsolutePath() + File.separator.toString() + BACKUP_DIR + File.separator.toString() + name
        if (!dbfile.endsWith(".db")) {
            dbfile += ".db"
        }
        val result = File(dbfile)
        if (!result.parentFile.exists()) {
            result.parentFile.mkdirs()
        }


        return result
    }

    /* this version is called for android devices >= api-11. thank to @damccull for fixing this. */
    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: CursorFactory,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase {
        return openOrCreateDatabase(name, mode, factory)
    }

    /* this version is called for android devices < api-11 */
    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: CursorFactory
    ): SQLiteDatabase {
        val result: SQLiteDatabase =
            SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null)
        // SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);

        return result
    }

    companion object {
        private const val DEBUG_CONTEXT = "DatabaseContext"
    }
}