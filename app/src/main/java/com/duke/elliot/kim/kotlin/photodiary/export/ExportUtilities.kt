package com.duke.elliot.kim.kotlin.photodiary.export

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.utility.MediaScanner
import com.duke.elliot.kim.kotlin.photodiary.utility.getDocumentDirectory
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter


object ExportUtilities {
    private lateinit var mediaScanner : MediaScanner

    /** weatherTexts must be initialized to match weather icon ids. */
    private lateinit var weatherWords: Array<String>

    /** The generated text file is saved in the Documents folder. */
    suspend fun exportAsTextFile(context: Context, diary: DiaryModel?, fileName: String) {
        if (diary == null)
            return

        withContext(Dispatchers.IO) {
            if (!::mediaScanner.isInitialized)
                mediaScanner = MediaScanner.newInstance(context)

            if (!::weatherWords.isInitialized)
                weatherWords = context.resources.getStringArray(R.array.weatherWords)

            try {
                val path = getDocumentDirectory(context)
                val directory = File(path, context.getString(R.string.app_name))
                if (!directory.exists())
                    directory.mkdir()

                val file = File(directory, "/${fileName}.txt")

                val fileWriter = FileWriter(file)

                // Write contents.
                val stringBuilder = StringBuilder()
                val date = diary.time.toDateFormat(context.getString(R.string.date_format))
                val time = diary.time.toDateFormat(context.getString(R.string.time_format))
                val weather = weatherWords[diary.weatherIconIndex]

                val dateTimeWeather = "$date $time $weather\n\n"
                val title = "${diary.title}\n\n"

                stringBuilder.append(dateTimeWeather)
                stringBuilder.append(title)
                stringBuilder.append(diary.content)

                fileWriter.write(stringBuilder.toString())

                mediaScanner.scanMedia(file.absolutePath)

                showToast(
                    context,
                    context.getString(R.string.text_file_created) + file.absolutePath
                )

                fileWriter.close()
            } catch (e: Exception) {
                Timber.e(e)
                showToast(context, context.getString(R.string.failed_to_create_text_file))
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    suspend fun exportAsTextFileQ(context: Context, diary: DiaryModel?, fileName: String) {
        if (diary == null)
            return

        withContext(Dispatchers.IO) {
            if (!::mediaScanner.isInitialized)
                mediaScanner = MediaScanner.newInstance(context)

            if (!::weatherWords.isInitialized)
                weatherWords = context.resources.getStringArray(R.array.weatherWords)

            try {
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS.toString() + "/${
                        context.getString(
                            R.string.app_name
                        )
                    }/"
                )
                val uri = context.contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    values
                )
                val outputStream = uri?.let { context.contentResolver.openOutputStream(it) }

                // Write contents.
                val stringBuilder = StringBuilder()
                val date = diary.time.toDateFormat(context.getString(R.string.date_format))
                val time = diary.time.toDateFormat(context.getString(R.string.time_format))
                val weather = weatherWords[diary.weatherIconIndex]

                val dateTimeWeather = "$date $time $weather\n\n"
                val title = "${diary.title}\n\n"

                stringBuilder.append(dateTimeWeather)
                stringBuilder.append(title)
                stringBuilder.append(diary.content)

                outputStream?.write(stringBuilder.toString().toByteArray())

                outputStream?.close()
                showToast(context, context.getString(R.string.text_file_created))
                mediaScanner.scanMedia(uri?.path ?: "")
            } catch (e: Exception) {
                showToast(context, context.getString(R.string.failed_to_create_text_file))
                Timber.e(e)
            }
        }
    }

    /** Share */
    fun sendDiary(activity: Activity, diary: DiaryModel) {
        val imageUris = diary.mediaArray.map { Uri.parse(it.uriString) } as ArrayList
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hello")
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        shareIntent.type = "image/jpeg"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(Intent.createChooser(shareIntent, "send"))
    }

}