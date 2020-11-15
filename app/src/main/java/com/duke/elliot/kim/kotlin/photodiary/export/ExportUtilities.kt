package com.duke.elliot.kim.kotlin.photodiary.export

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
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
import java.util.*
import kotlin.collections.ArrayList


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

                val dateTimeWeather = "$date  $time  $weather\n\n"
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
        if (!::weatherWords.isInitialized)
            weatherWords = activity.resources.getStringArray(R.array.weatherWords)

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)

        val imageUris = diary.mediaArray.map { toContentUri(activity, it.uriString) } as ArrayList

        val stringBuilder = StringBuilder()
        val date = diary.time.toDateFormat(activity.getString(R.string.date_format))
        val time = diary.time.toDateFormat(activity.getString(R.string.time_format))
        val weather = weatherWords[diary.weatherIconIndex]
        val dateTimeWeather = "$date  $time  $weather\n\n"

        stringBuilder.append(dateTimeWeather)
        stringBuilder.append(diary.title + "\n\n")
        stringBuilder.append(diary.content)

        shareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        shareIntent.type = "image/*"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooserIntent = getIntentChooser(
            activity,
            shareIntent,
            activity.getString(R.string.share_diary)
        )

        activity.startActivity(chooserIntent)
    }

    private fun toContentUri(activity: Activity, uriString: String): Uri? {
        return  FileProvider.getUriForFile(
            activity,
            activity.applicationContext.packageName.toString() + ".fileprovider",
            File(Uri.parse(uriString).path!!)
        )
    }

    private fun getIntentChooser(
        context: Context,
        intent: Intent,
        chooserTitle: CharSequence? = null
    ): Intent? {
        if (!::weatherWords.isInitialized)
            weatherWords = context.resources.getStringArray(R.array.weatherWords)

        val exclusionList = listOf("com.facebook.katana", "com.kakao.talk")

        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
        val excludedComponentNames = HashSet<ComponentName>()

        resolveInfoList.forEach {
            val activityInfo = it.activityInfo
            val packageName = activityInfo.packageName

            if (exclusionList.contains(packageName.toLowerCase(Locale.ROOT)))
                excludedComponentNames.add(
                    ComponentName(
                        activityInfo.packageName,
                        activityInfo.name
                    )
                )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Intent.createChooser(intent, chooserTitle).putExtra(
                Intent.EXTRA_EXCLUDE_COMPONENTS,
                excludedComponentNames.toTypedArray()
            )

        if (resolveInfoList.isNotEmpty()) {
            val includedIntents: MutableList<Intent> = ArrayList()
            for (resolveInfo in resolveInfoList) {
                val activityInfo = resolveInfo.activityInfo

                if (excludedComponentNames.contains(
                        ComponentName(
                            activityInfo.packageName,
                            activityInfo.name
                        )
                    ))
                    continue

                val includedIntent = Intent(intent)
                includedIntent.setPackage(activityInfo.packageName)
                includedIntent.component = ComponentName(
                    activityInfo.packageName,
                    activityInfo.name
                )

                val labeledIntent = LabeledIntent(
                    includedIntent,
                    activityInfo.packageName,
                    resolveInfo.labelRes,
                    resolveInfo.icon
                )
                includedIntents.add(labeledIntent)
            }

            val chooserIntent: Intent?
            chooserIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                Intent.createChooser(Intent(), chooserTitle)
            else
                Intent.createChooser(includedIntents.removeAt(0), chooserTitle)

            if (chooserIntent == null)
                return null

            // add initial intents
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                includedIntents.toTypedArray<Parcelable>()
            )
            return chooserIntent
        }

        return null
    }

    // TODO; 바로 보내지는 지 test 카톡.
    fun sendDiaryToKakaoTalk(activity: Activity, diary: DiaryModel) {
        if (!::weatherWords.isInitialized)
            weatherWords = activity.resources.getStringArray(R.array.weatherWords)

        val shareIntent = Intent(Intent.ACTION_SEND)

        val imageUris = diary.mediaArray.map { toContentUri(activity, it.uriString) } as ArrayList

        val stringBuilder = StringBuilder()
        val date = diary.time.toDateFormat(activity.getString(R.string.date_format))
        val time = diary.time.toDateFormat(activity.getString(R.string.time_format))
        val weather = weatherWords[diary.weatherIconIndex]
        val dateTimeWeather = "$date  $time  $weather\n\n"

        stringBuilder.append(dateTimeWeather)
        stringBuilder.append(diary.title + "\n\n")
        stringBuilder.append(diary.content)

        shareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
        // shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        shareIntent.type = "text/plain"
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setPackage("com.kakao.talk") // 존나 잘됨.

        activity.startActivity(shareIntent)
    }

    fun showKakaoTalkSendOptions(context: Context) {
        if (isKakaoTalkInstalled(context)) {

            // 이미지 연속 전송.
            // 텍스트 온리.
            // 동영상 1개,
            // 오디오 1개.
        }
    }

    // TODO: Test
    fun sendDiaryToFacebook(activity: Activity, diary: DiaryModel) {
        if (!::weatherWords.isInitialized)
            weatherWords = activity.resources.getStringArray(R.array.weatherWords)

        val shareIntent = Intent(Intent.ACTION_SEND)

        val imageUris = diary.mediaArray.map { toContentUri(activity, it.uriString) } as ArrayList

        val stringBuilder = StringBuilder()
        val date = diary.time.toDateFormat(activity.getString(R.string.date_format))
        val time = diary.time.toDateFormat(activity.getString(R.string.time_format))
        val weather = weatherWords[diary.weatherIconIndex]
        val dateTimeWeather = "$date  $time  $weather\n\n"

        stringBuilder.append(dateTimeWeather)
        stringBuilder.append(diary.title + "\n\n")
        stringBuilder.append(diary.content)

        shareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
        // shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        shareIntent.type = "text/plain"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setPackage("com.facebook.katana")

        activity.startActivity(shareIntent)
    }

    private fun isKakaoTalkInstalled(context: Context): Boolean {
        val applicationInfoList = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (applicationInfo in applicationInfoList)
            if (applicationInfo.packageName == "com.kakao.talk")
                return true

        return false
    }

    private fun isFacebookInstalled(context: Context): Boolean {
        val applicationInfoList = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (applicationInfo in applicationInfoList)
            if (applicationInfo.packageName == "com.facebook.katana")
                return true

        return false
    }
}