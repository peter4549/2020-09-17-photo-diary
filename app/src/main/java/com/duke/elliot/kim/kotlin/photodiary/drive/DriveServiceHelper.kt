package com.duke.elliot.kim.kotlin.photodiary.drive

import android.content.Context
import android.net.Uri
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.DIARY_DATABASE_NAME
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.getCurrentTime
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import timber.log.Timber
import java.io.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

private const val BACKUP_METADATA_FILE_NAME = "backup_metadata.txt"

class DriveServiceHelper(private val drive: Drive) {

    private val executor = Executors.newFixedThreadPool(10)
    private val metadataExecutor = Executors.newSingleThreadExecutor()

    fun restore(
        context: Context,
        completeListener: ((result: Boolean, downloadedFilePaths: List<String>?) -> Unit)?
    ) {
        var executeFailed = false
        val files = drive.files().list()
            .setQ("name != '${BACKUP_METADATA_FILE_NAME}'")
            .setSpaces("appDataFolder")
            .execute().files

        val futures = mutableListOf<Future<String?>?>()

        for (file in files) {
            futures.add(executor.submit<String> {
                try {
                    val id = file.id
                    val name = file.name

                    val outputStream: FileOutputStream
                    var path: String

                    if (name.contains(DIARY_DATABASE_NAME) && !name.contains(".")) {
                        path = context.getDatabasePath(DIARY_DATABASE_NAME).path
                        if (name.endsWith("-shm"))
                            path += "-shm"

                        if (name.endsWith("-wal"))
                            path += "-wal"
                    } else
                        path = context.getExternalFilesDir(null)?.path + "/$name"

                    outputStream = FileOutputStream(path)

                    drive.files().get(id).executeMediaAsInputStream().use { inputStream ->
                        if (copyFile(inputStream, outputStream))
                            path
                        else
                            null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "File restore failed: ${e.message}")
                    e.printStackTrace()
                    null
                }
            })
        }

        executor.shutdown()

        if (executor.awaitTermination(120, TimeUnit.MINUTES)) {
            Timber.d("Backup success.")
        } else {
            Timber.d("Backup failure.")
            executeFailed = true
            executor.shutdownNow()
        }

        val downloadedMediaPaths = futures.map { it?.get() }.requireNoNulls()
        if (executeFailed)
            completeListener?.invoke(false, downloadedMediaPaths)
        else {
            val backedUpFileCount = files.count()
            val downloadedFileCount = futures.map { it?.get() }.count()

            if (downloadedFileCount == backedUpFileCount)
                completeListener?.invoke(true, null)
            else
                completeListener?.invoke(false, downloadedMediaPaths)
        }
    }

    fun backup(
        context: Context, mediaList: List<MediaModel>,
        completeListener: ((result: Boolean, errorMessage: String?) -> Unit)?
    ) {
        var executeFailed = false
        val futures = mutableListOf<Future<String?>?>()
        val databaseFilePaths = arrayOf(
            DIARY_DATABASE_NAME,
            "$DIARY_DATABASE_NAME-shm",
            "$DIARY_DATABASE_NAME-wal"
        )

        try {
            val future = executor.submit<List<String>> {
                drive.files().list()
                    .setSpaces("appDataFolder")
                    .execute().files.map { it.id }
            }

            /** Database */
            for (databaseFilePath in databaseFilePaths) {
                futures.add(executor.submit<String> {
                    backupDatabaseFile(
                        context.getDatabasePath(databaseFilePath), databaseFilePath
                    )
                })
            }

            /** Media */
            for (media in mediaList) {
                futures.add(executor.submit<String> {
                    backupMediaFile(media)
                })
            }

            executor.shutdown()

            if (executor.awaitTermination(120, TimeUnit.MINUTES)) {
                Timber.d("Backup success.")
            } else {
                Timber.d("Backup failure.")
                executeFailed = true
                executor.shutdownNow()
            }

            val existingBackedUpMediaFileIds = future.get() ?: listOf()
            val backedUpFileIds = futures.map { it?.get() }

            if (backedUpFileIds.contains(null))
                executeFailed = true

            if (executeFailed) { // Failure
                completeListener?.invoke(false, null)
                deleteGoogleDriveFiles(backedUpFileIds.requireNoNulls())
            } else { // Success
                completeListener?.invoke(true, null)
                deleteGoogleDriveFiles(existingBackedUpMediaFileIds)
            }
        } catch (e: Exception) {
            Timber.e(e)
            completeListener?.invoke(false, e.message)
        }
    }

    private fun deleteGoogleDriveFiles(fileIds: List<String>) {
        for (fileId in fileIds) {
            Timber.d("Google Drive File Deleted: File ID: $fileId")
            drive.files().delete(fileId).execute()
        }
    }

    /**
     * Mime Types Reference:
     * https://developers.google.com/drive/api/v3/mime-types
     */
    private fun backupDatabaseFile(backupDatabaseFile: java.io.File, name: String): String? {
        val metadata = File()
            .setParents(Collections.singletonList("appDataFolder"))
            .setMimeType("application/x-sqlite3")
            .setName(name)

        val fileContent = FileContent("application/x-sqlite3", backupDatabaseFile)

        val file = drive.files().create(metadata, fileContent)
            .setFields("id")
            .execute()
            ?: throw IOException("Null result when requesting file creation.")

        return file.id
    }

    private fun backupMediaFile(media: MediaModel): String? {
        try {
            val mediaFile = File(Uri.parse(media.uriString).path ?: return null)

            val mimeType = when (media.type) {
                MediaHelper.MediaType.PHOTO -> "image/*"
                MediaHelper.MediaType.VIDEO -> "video/*"
                else -> "*/*"
            }

            val metadata = File()
                .setParents(Collections.singletonList("appDataFolder"))
                .setMimeType(mimeType)
                .setName(mediaFile.name)

            val fileContent = FileContent(mimeType, mediaFile)

            val googleFile = drive.files().create(metadata, fileContent)
                .setFields("id")
                .execute()
                ?: throw IOException("Null result when requesting file creation.")

            return googleFile.id
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @Suppress("unused")
    fun showBackedUpFiles() {
        val files: FileList = drive.files().list()
            .setSpaces("appDataFolder")
            .execute()
        println("showBackedUpFiles")
        println("Backed up files:")
        for ((index, file) in files.files.withIndex()) {
            println(
                "${index + 1}: File ID: ${file.id} File Name: ${file.name}"
            )
        }
    }

    private fun copyFile(inputStream: InputStream, outputStream: OutputStream): Boolean {
        return try {
            inputStream.copyTo(outputStream)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createMetadata(context: Context, diaryCount: Long): Task<String> =
        Tasks.call(metadataExecutor, {
            val metadataFiles = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_METADATA_FILE_NAME'")
                .execute().files

            for (metadataFile in metadataFiles) {
                drive.files().delete(metadataFile.id)
            }

            val fileMetadata = File()
            fileMetadata.parents = Collections.singletonList("appDataFolder")
            fileMetadata.name = BACKUP_METADATA_FILE_NAME
            fileMetadata.mimeType = "text/plain"

            val stringBuilder = StringBuilder()
            stringBuilder.append(
                "${context.getString(R.string.last_backup_date)}: ${
                    getCurrentTime().toDateFormat(
                        "yyyy.M.d  aa h:m"
                    )
                }_"
            )
            stringBuilder.append("${context.getString(R.string.last_backup_device)}: ${android.os.Build.MODEL}_")
            stringBuilder.append("${context.getString(R.string.backed_up_diary_count)}: $diaryCount")

            val contentStream = ByteArrayContent.fromString ("text/plain", stringBuilder.toString())

            drive.files().create(fileMetadata, contentStream).execute().id
        })

    fun readMetadata(): Task<String?>? {
        return Tasks.call(metadataExecutor, {

            val metadataFiles = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_METADATA_FILE_NAME'")
                .execute().files

            val metadataFileId = metadataFiles[0].id ?: return@call null

            drive.files().get(metadataFileId).executeMediaAsInputStream().use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }

                    return@call stringBuilder.toString().replace("_", "\n")
                }
            }
        })
    }

}