package com.duke.elliot.kim.kotlin.photodiary.drive

import android.content.Context
import android.net.Uri
import com.duke.elliot.kim.kotlin.photodiary.database.DIARY_DATABASE_NAME
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.Operation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


class DriveServiceHelper(private val drive: Drive) {

    private lateinit var operation: Operation<String>
    private val executor = Executors.newSingleThreadExecutor()

    fun restore(context: Context, completeListener: ((result: Boolean, downloadedFilePaths: List<String>?) -> Unit)?) {
        // TODO: 현재 데이터 임시저장 로직.

        var executeFailed = false
        val files = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("nextPageToken, files(id, name)")
            .setPageSize(10)
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

        if(executor.awaitTermination(60, TimeUnit.MINUTES)) {
            Timber.d("Backup success.")
        } else {
            Timber.d("Backup failure.")
            executeFailed = true
            executor.shutdownNow()
        }

        val downloadedMediaPaths = futures.map { it?.get() }.requireNoNulls() // 성공한 애들이 담긴다.
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

    fun setOperation(operation: Operation<String>) {
        this.operation = operation
    }

    /**
     * Mime Types Reference:
     * https://developers.google.com/drive/api/v3/mime-types
     */
    private fun backupDatabaseFile(backupDatabaseFile: java.io.File?, name: String) = runBlocking {
            val metadata = File()
                .setParents(Collections.singletonList("appDataFolder"))
                .setMimeType("application/x-sqlite3")
                .setName(name)

            val fileContent = FileContent("application/x-sqlite3", backupDatabaseFile)

            val googleFile = drive.files().create(metadata, fileContent)
                .setFields("id")
                .execute()
                ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
    }

    suspend fun backupDatabaseFiles(context: Context): String? {
        withContext(Dispatchers.Default) {
            backupDatabaseFile(
                context.getDatabasePath(DIARY_DATABASE_NAME),
                DIARY_DATABASE_NAME
            )
        } ?: return null

        withContext(Dispatchers.Default) {
            backupDatabaseFile(
                context.getDatabasePath("$DIARY_DATABASE_NAME-shm"),
                "$DIARY_DATABASE_NAME-shm"
            )
        } ?: return null

        return withContext(Dispatchers.Default) {
            backupDatabaseFile(
                context.getDatabasePath("$DIARY_DATABASE_NAME-wal"),
                "$DIARY_DATABASE_NAME-wal"
            )
        } ?: null
    }

    private fun backupMediaFile(media: MediaModel) = runBlocking {
        try {
            val mediaFile = File(Uri.parse(media.uriString).path ?: return@runBlocking null)

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

            googleFile.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun backupMediaFiles(mediaList: List<MediaModel>): Boolean {
        return withContext(Dispatchers.Default) {
            for (media in mediaList) {
                backupMediaFile(media) ?: return@withContext false
            }
            return@withContext true
        }
    }


    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * contents.
     */
    fun restore1(context: Context, id: String, name: String): Task<String> { //Task<Pair<String?, String?>>? {
        return Tasks.call(executor) {

            // Retrieve the metadata as a File object.
            // val metadata = drive.files().get(fileId).execute() // TODO. mark
           //  val name: String = metadata.name

            var outputStream: FileOutputStream
            if (name.contains(DIARY_DATABASE_NAME) && !name.contains(".")) {
                var databaseFilePath = context.getDatabasePath(DIARY_DATABASE_NAME).path
                if (name.endsWith("-shm"))
                    databaseFilePath += "-shm"

                if (name.endsWith("-wal"))
                    databaseFilePath += "-wal"

                outputStream = FileOutputStream(databaseFilePath)
            } else
                outputStream = FileOutputStream(context.getExternalFilesDir(null)?.path + "/$name")


            drive.files().get(id).executeMediaAsInputStream().use { inputStream ->
                copyFile(inputStream, outputStream)

            }
            "a"
        }
    }

    @Suppress("unused")
    fun queryFiles(): Task<FileList?>? {
        return Tasks.call(executor) { drive.files().list().setSpaces("drive").execute() }
    }

    fun showAppFiles() {
        val files: FileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("nextPageToken, files(id, name)")
            .setPageSize(10)
            .execute()
        for (file in files.files) {
            println(
                "Found file: ${file.name} ${file.id} oooooooooo"
            )

            // drive.files().delete(file.id).execute() // work.
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
}