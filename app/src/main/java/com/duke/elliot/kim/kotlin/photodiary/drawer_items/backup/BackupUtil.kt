package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.content.Context
import android.os.Environment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.DIARY_DATABASE_NAME
import com.duke.elliot.kim.kotlin.photodiary.database.DatabaseOpenHelper
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

const val DATA_BACKUP_FILE_NAME = "diary_backup.db"
const val BACKUP_DIR = "ChouChouDiary/backup"
const val DATABASE_NAME_WITH_FORMAT = "$DIARY_DATABASE_NAME"

object BackupUtil {
    fun backupToInternalStorage(context: Context) {
        val database: DiaryDatabase = DiaryDatabase.getInstance(context)
        // database.close()
        // database.openHelper.close()

        val databaseFile = File(context.getDatabasePath(DIARY_DATABASE_NAME).absolutePath)
        val saveDir = File(Environment.getExternalStorageDirectory(), BACKUP_DIR)
        val fileName = DIARY_DATABASE_NAME
        val saveFilePath = saveDir.path + File.separator.toString() + fileName

        if (!saveDir.exists())
            saveDir.mkdirs()

        val saveFile = File(saveFilePath)

        if (saveFile.exists()) {
            saveFile.delete()
        }

        try {
            if (saveFile.createNewFile()) {
                val bufferSize = 8 * 1024
                val buffer = ByteArray(bufferSize)
                var bytesRead = bufferSize
                val fileOutputStream = FileOutputStream(saveFilePath)
                val fileInputStream = FileInputStream(databaseFile)
                while (fileInputStream.read(buffer, 0, bufferSize).also { bytesRead = it } > 0) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
                fileOutputStream.flush()
                fileInputStream.close()
                fileOutputStream.close()
                // TODO, change, information of backup will be stored.
                /*
                val sharedPreferences: SharedPreferences =
                    context.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
                sharedPreferences.edit().putString("backupFileName", fileName).apply()
                updateLastBackupTime(sharedPreferences)

                 */
                showToast(context, context.getString(R.string.data_backed_up))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
        }

        // database.openHelper.writableDatabase
    }

    fun restoreInternalStorageDataBackupFile(context: Context) {
        /** test */
        if (DatabaseOpenHelper(context, DIARY_DATABASE_NAME).importDatabase(context))
            showToast(context, "TRUE")
        else
            showToast(context, "FFFFF")
    }

    fun copyFile(fromFile: FileInputStream, toFile: FileOutputStream) {
        var fromChannel: FileChannel? = null
        var toChannel: FileChannel? = null
        try {
            fromChannel = fromFile.channel
            toChannel = toFile.channel
            fromChannel.transferTo(0, fromChannel.size(), toChannel)
        } finally {
            try {
                fromChannel?.close()
            } finally {
                toChannel?.close()
            }
        }
    }

    fun exportDatabaseFile(context: Context) {

        try {
            copyDataFromOneToAnother(context.getDatabasePath(DATABASE_NAME_WITH_FORMAT).path, Environment.getExternalStorageDirectory().path + "/Download/" + "backup_" + DATABASE_NAME_WITH_FORMAT)
            copyDataFromOneToAnother(context.getDatabasePath(DATABASE_NAME_WITH_FORMAT + "-shm").path, Environment.getExternalStorageDirectory().path + "/Download/" + "backup_" + DATABASE_NAME_WITH_FORMAT + "-shm")
            copyDataFromOneToAnother(context.getDatabasePath(DATABASE_NAME_WITH_FORMAT + "-wal").path, Environment.getExternalStorageDirectory().path + "/Download/" + "backup_" + DATABASE_NAME_WITH_FORMAT + "-wal")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importDatabaseFile(context: Context) {
        try {
            copyDataFromOneToAnother(Environment.getExternalStorageDirectory().path + "/Download/" + "backup_" + DATABASE_NAME_WITH_FORMAT, context.getDatabasePath(DATABASE_NAME_WITH_FORMAT).path)
            copyDataFromOneToAnother(Environment.getExternalStorageDirectory().path + "/Download/" + "backup_" + DATABASE_NAME_WITH_FORMAT + "-shm", context.getDatabasePath(DATABASE_NAME_WITH_FORMAT + "-shm").path)
            copyDataFromOneToAnother(Environment.getExternalStorageDirectory().path + "/Download/" + "backup_" + DATABASE_NAME_WITH_FORMAT + "-wal", context.getDatabasePath(DATABASE_NAME_WITH_FORMAT + "-wal").path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyDataFromOneToAnother(fromPath: String, toPath: String) {
        val inStream = File(fromPath).inputStream()
        val outStream = FileOutputStream(toPath)

        inStream.use { input ->
            outStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}