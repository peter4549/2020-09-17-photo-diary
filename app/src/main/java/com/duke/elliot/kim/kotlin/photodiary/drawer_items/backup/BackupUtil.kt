package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import com.duke.elliot.kim.kotlin.photodiary.database.DIARY_DATABASE_NAME
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

const val BACKUP_DIR_PATH = "/DayStory/"
const val BACKUP_FILE_NAME = "backup_day_story.zip"

private const val BUFFER = 2048

object BackupUtil {
    fun backupToInternalZipFile(context: Context, files: List<String>, zipDirPath: String, zipFileName: String): Boolean {
        try {
            val zipDir = File(zipDirPath)

            if (!zipDir.exists())
                zipDir.mkdir()

            val zipFilePath = zipDirPath + zipFileName
            val zipFile = File(zipFilePath)

            if (zipFile.exists())
                zipFile.delete()

            if (!zipFile.exists())
                zipFile.createNewFile()

            var src: BufferedInputStream?
            val dst = FileOutputStream(zipFile)
            val zipOutputStream = ZipOutputStream(BufferedOutputStream(dst))
            val data = ByteArray(BUFFER)

            val mutableFiles = mutableListOf<String>()
            mutableFiles.addAll(files)
            mutableFiles.add(context.getDatabasePath(DIARY_DATABASE_NAME).path)
            mutableFiles.add(context.getDatabasePath("$DIARY_DATABASE_NAME-shm").path)
            mutableFiles.add(context.getDatabasePath("$DIARY_DATABASE_NAME-wal").path)

            for (file in mutableFiles) {
                val fileInputStream = FileInputStream(Uri.parse(file).path)
                src = BufferedInputStream(fileInputStream, BUFFER)
                val entry = ZipEntry(file.substring(file.lastIndexOf("/") + 1))
                zipOutputStream.putNextEntry(entry)
                var count: Int
                while (src.read(data, 0, BUFFER).also { count = it } != -1) {
                    zipOutputStream.write(data, 0, count)
                }
                src.close()
            }

            zipOutputStream.close()
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun restoreFromInternalZipFile(context: Context, backupFilePath: String) {
        try {
            val fileInputStream = FileInputStream(backupFilePath)
            val zipInputStream = ZipInputStream(fileInputStream)
            var zipEntry: ZipEntry?
            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                var fileOutputStream: FileOutputStream?
                if (zipEntry?.name?.contains(DIARY_DATABASE_NAME) == true) {
                    var databaseFilePath = context.getDatabasePath(DIARY_DATABASE_NAME).path
                    if (zipEntry?.name?.endsWith("-shm") == true)
                        databaseFilePath += "-shm"

                    if (zipEntry?.name?.endsWith("-wal") == true)
                        databaseFilePath += "--wal"

                    fileOutputStream = FileOutputStream(databaseFilePath)
                } else
                    fileOutputStream = FileOutputStream(
                        context.getExternalFilesDir(null)
                            .toString() + "/${zipEntry?.name ?: throw Exception("Entry name not found.")}"
                    )

                val buffer = ByteArray(BUFFER)
                var length: Int
                while (zipInputStream.read(buffer).also { length = it } > 0) {
                    fileOutputStream.write(buffer, 0, length)
                }

                zipInputStream.closeEntry()
                fileOutputStream.close()
            }

            zipInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Above Q */
    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.Q)
    fun backupToInternalZipFileQ(context: Context, files: List<String>): Boolean {
        try {

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH
            )

            val query = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                null
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val relativePath = cursor.getString(relativePathColumn)

                    if (displayName == BACKUP_FILE_NAME &&
                        mimeType == "application/zip" &&
                        relativePath.contains("DayStory")) {
                        val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"),
                            id
                        )

                        val outputStream = contentUri.let {
                            context.contentResolver.openOutputStream(it)
                        } ?: throw Exception("outputStream is null.")

                        writeZipFile(context, outputStream, files)
                        return true
                    }
                }
            }

            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, BACKUP_FILE_NAME)
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS.toString() + "/DayStory/")

            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                values
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)
            }.use { outputStream ->
                writeZipFile(context, outputStream ?: throw Exception("outputStream is null."), files)
            }

            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun writeZipFile(context: Context, outputStream: OutputStream, files: List<String>) {
        var src: BufferedInputStream?
        val zipOutputStream = ZipOutputStream(BufferedOutputStream(outputStream))
        val data = ByteArray(BUFFER)

        val mutableFiles = mutableListOf<String>()
        mutableFiles.addAll(files)
        mutableFiles.add(context.getDatabasePath(DIARY_DATABASE_NAME).path)
        mutableFiles.add(context.getDatabasePath("$DIARY_DATABASE_NAME-shm").path)
        mutableFiles.add(context.getDatabasePath("$DIARY_DATABASE_NAME-wal").path)

        for (file in mutableFiles) {
            val fileInputStream = FileInputStream(Uri.parse(file).path)
            src = BufferedInputStream(fileInputStream, BUFFER)
            val entry = ZipEntry(file.substring(file.lastIndexOf("/") + 1))
            zipOutputStream.putNextEntry(entry)
            var count: Int

            while (src.read(data, 0, BUFFER).also { count = it } != -1) {
                zipOutputStream.write(data, 0, count)
            }

            src.close()
        }

        zipOutputStream.close()
    }
}