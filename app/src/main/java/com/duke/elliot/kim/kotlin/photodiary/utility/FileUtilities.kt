package com.duke.elliot.kim.kotlin.photodiary.utility

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.room.Room
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.setConfigure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class FileUtilities private constructor (private val context: Context) {

    @SuppressLint("NewApi", "ObsoleteSdkInt", "Recycle")
    fun getPath(uri: Uri): String? {
        // Check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        var selection: String? = null
        var selectionArgs: Array<String>? = null

        // DocumentProvider
        if (isKitKat) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val documentId = DocumentsContract.getDocumentId(uri)
                val split = documentId.split(":".toRegex()).toTypedArray()
                val fullPath = getPathFromExtSD(split)
                return if (fullPath !== "")
                    fullPath
                else
                    null
            }

            // DownloadsProvider
            if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var cursor: Cursor? = null
                    try {
                        cursor = context.contentResolver.query(
                            uri,
                            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )
                        if (cursor != null && cursor.moveToFirst()) {
                            val fileName: String = cursor.getString(0)
                            val path: String = context.getExternalFilesDir(null).toString() +
                                    "/Download/" + fileName
                            if (!TextUtils.isEmpty(path))
                                return path
                        }
                    } finally {
                        cursor?.close()
                    }
                    val id: String = DocumentsContract.getDocumentId(uri)
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                        )
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            return try {
                                val contentUri: Uri = ContentUris.withAppendedId(
                                    Uri.parse(contentUriPrefix),
                                    java.lang.Long.valueOf(id)
                                )
                                getDataColumn(context, contentUri, null, null)
                            } catch (e: NumberFormatException) {
                                // In Android 8 and Android P the id is not a number
                                uri.path?.replaceFirst("^/document/raw:", "")
                                    ?.replaceFirst("^raw:", "")
                            }
                        }
                    }
                } else {
                    val documentId = DocumentsContract.getDocumentId(uri)
                    if (documentId.startsWith("raw:")) {
                        return documentId.replaceFirst("raw:".toRegex(), "")
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(documentId)
                        )
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }

                    return getDataColumn(context, contentUri ?: return null, null, null)
                }
            }

            // MediaProvider
            if (isMediaDocument(uri)) {
                val documentId = DocumentsContract.getDocumentId(uri)
                val split = documentId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                val contentUri: Uri
                contentUri = when (type) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> return null
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
                return getDataColumn(
                    context, contentUri, selection,
                    selectionArgs
                )
            }
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri)
            }
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(uri)
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri)
                }
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    copyFileToInternalStorage(uri, "userfiles")
                } else {
                    getDataColumn(context, uri, null, null)
                }
            }
            if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        } else {
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(uri)
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                val projection = arrayOf(
                    MediaStore.Images.Media._ID
                )
                val cursor: Cursor
                try {
                    cursor = context.contentResolver
                        .query(uri, projection, selection, selectionArgs, null) ?: return null
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    if (cursor.moveToFirst())
                        return columnIndex.let { cursor.getString(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    private fun getPathFromExtSD(pathData: Array<String>): String {
        val type = pathData[0]
        val relativePath = "/" + pathData[1]
        var fullPath = ""

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equals(type, ignoreCase = true)) {
            fullPath = context.getExternalFilesDir(null).toString() + relativePath
            if (fileExists(fullPath))
                return fullPath
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
        if (fileExists(fullPath))
            return fullPath
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
        return if (fileExists(fullPath))
            fullPath
        else fullPath
    }

    @SuppressLint("Recycle")
    private fun getDriveFilePath(uri: Uri): String? {
        val returnUri: Uri = uri
        val returnCursor =
            context.contentResolver.query(returnUri, null, null, null, null) ?: return null
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.cacheDir, name ?: return null)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream?.available()

            val bufferSize = min(bytesAvailable ?: 0, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null)
                        read = it
                } != -1) {
                outputStream.write(buffers, 0, read)
            }

            Timber.tag("File size").e("Size ${file.length()}")
            inputStream?.close()
            outputStream.close()
            Timber.tag("File path").e("Path ${file.path}")
            Timber.tag("File size").e("Size ${file.length()}")
        } catch (e: Exception) {
            Timber.e(e)
        }
        return file.path
    }

    /**
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    @SuppressLint("Recycle")
    private fun copyFileToInternalStorage(uri: Uri, newDirName: String): String? {
        val returnUri: Uri = uri
        val returnCursor = context.contentResolver.query(
            returnUri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        ) ?: return null

        /**
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         */
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val output: File
        output = if (newDirName != "") {
            val dir = File(context.filesDir.toString() + "/" + newDirName)
            if (!dir.exists()) {
                dir.mkdir()
            }
            File(context.filesDir.toString() + "/" + newDirName + "/" + name)
        } else {
            File(context.filesDir.toString() + "/" + name)
        }
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null)
                        read = it
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            Timber.e(e)
        }
        return output.path
    }

    @SuppressLint("Recycle")
    suspend fun copyFileToInternalStorage(sourceUri: Uri, prefix: String = "", suffix: String = ""): Uri? {
        return withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            sourceUri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        ) ?: return@withContext null

        /**
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         */
        val displayNameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        val displayName: String = cursor.getString(displayNameIndex)
        val size = cursor.getLong(sizeIndex).toString()

        val fileName = displayName.substringBeforeLast(".")

        var newDisplayName = displayName.replace(fileName, "${fileName}$suffix")
        if (prefix != "")
            newDisplayName = prefix + newDisplayName

        replaceLast(displayName, fileName, "${prefix}$fileName${suffix}")

        val outputFile = File(context.getExternalFilesDir(null).toString() + "/${newDisplayName}")

            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(outputFile)
                var read = 0
                val bufferSize = 1024
                val buffers = ByteArray(bufferSize)
                while (inputStream?.read(buffers).also {
                        if (it != null)
                            read = it
                    } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream.close()
            } catch (e: Exception) {
                Timber.e(e)
                return@withContext null
            }


            return@withContext outputFile.toUri()
        }
    }

    private fun getFilePathForWhatsApp(uri: Uri): String? {
        @Suppress("SpellCheckingInspection")
        return copyFileToInternalStorage(uri, "whatsapp")
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri, projection,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getBitmapFromUri(imageUri: Uri?): Bitmap? {
        try {
            imageUri?.let {
                return if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        imageUri
                    ).setConfigure(Bitmap.Config.ARGB_8888)
                } else {
                    val source =
                        ImageDecoder.createSource(
                            context.contentResolver,
                            imageUri
                        )
                    ImageDecoder.decodeBitmap(source)
                        .setConfigure(Bitmap.Config.ARGB_8888)
                }
            } ?: run {
                Timber.e("Image not found")
                return null
            }
        } catch (e: Exception) {
            Timber.e(e)
            return null
        }
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        @Suppress("SpellCheckingInspection")
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    @Suppress("SpellCheckingInspection")
    fun isWhatsAppFile(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority ||
                "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    companion object {
        private var contentUri: Uri? = null

        @Volatile
        private var INSTANCE: FileUtilities? = null

        fun getInstance(context: Context): FileUtilities {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = FileUtilities(context)

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}