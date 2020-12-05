package com.duke.elliot.kim.kotlin.photodiary.export

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.duke.elliot.kim.kotlin.photodiary.MainViewModel
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import com.facebook.share.model.*
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.item_select_dialog_28.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList

const val EXPORT_REQUEST_CODE = 9001

object ExportUtilities {
    const val KAKAO_TALK_OPTION_SEND_IMAGES = 0
    const val KAKAO_TALK_OPTION_SEND_VIDEO = 1
    const val KAKAO_TALK_OPTION_SEND_AUDIO = 2
    const val KAKAO_TALK_OPTION_SEND_TEXT = 3

    private const val EXPORT_AS_TEXT_FILE = 0
    private const val EXPORT_AS_PDF_FILE = 1
    private const val SHARE_DIARY = 2
    private const val SEND_DIARY_TO_KAKAO_TALK = 3
    private const val SEND_DIARY_TO_FACEBOOK = 4

    private lateinit var mediaScanner : MediaScanner

    /** weatherTexts must be initialized to match weather icon ids. */
    private lateinit var weatherWords: Array<String>

    /** The generated text file is saved in the Documents folder. */
    suspend fun exportAsTextFile(context: Context, diary: DiaryModel?, fileName: String) {
        if (diary == null)
            return

        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(Dispatchers.IO) {
            if (!::mediaScanner.isInitialized)
                mediaScanner = MediaScanner.newInstance(context)

            if (!::weatherWords.isInitialized)
                weatherWords = context.resources.getStringArray(R.array.weatherWords)

            try {
                val path = getDocumentDirectory()
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

        @Suppress("BlockingMethodInNonBlockingContext")
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
                        "ChouChouDiary"
                    }/"
                )
                val uri = context.contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    values
                )
                val outputStream = uri?.let {
                    context.contentResolver.openOutputStream(it)
                }

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

        activity.startActivityForResult(chooserIntent, EXPORT_REQUEST_CODE)
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

    fun sendDiaryToKakaoTalk(
        activity: Activity, diary: DiaryModel, option: Int,
        mediaUris: List<String>? = null, mediaUri: String? = null,
        afterSendCallback: (() -> Unit)? = null
    ) {
        if (!::weatherWords.isInitialized)
            weatherWords = activity.resources.getStringArray(R.array.weatherWords)

        if (!isKakaoTalkInstalled(activity)) {
            showToast(activity, activity.getString(R.string.kakao_talk_not_found))
            return
        }


        when(option) {
            KAKAO_TALK_OPTION_SEND_IMAGES -> sendPhotosToKakaoTalk(activity, mediaUris)
            KAKAO_TALK_OPTION_SEND_VIDEO -> sendVideoToKakaoTalk(activity, mediaUri)
            KAKAO_TALK_OPTION_SEND_AUDIO -> sendAudioToKakaoTalk(activity, mediaUri)
            KAKAO_TALK_OPTION_SEND_TEXT -> sendTextToKakaoTalk(activity, diary)
        }

        afterSendCallback?.invoke()
    }

    private fun sendPhotosToKakaoTalk(activity: Activity, photoUris: List<String>?) {
        if (photoUris == null)
            return

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)

        shareIntent.type = "image/*"
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, photoUris.map {
            toContentUri(
                activity,
                it
            )
        } as ArrayList)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setPackage("com.kakao.talk")

        activity.startActivityForResult(
            Intent.createChooser(
                shareIntent,
                activity.getString(R.string.send_diary_to_kakao_talk)
            ), EXPORT_REQUEST_CODE
        )
    }

    private fun sendVideoToKakaoTalk(activity: Activity, videoUri: String?) {
        if (videoUri == null)
            return

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, toContentUri(activity, videoUri))
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setPackage("com.kakao.talk")

        activity.startActivityForResult(
            Intent.createChooser(
                shareIntent,
                activity.getString(R.string.send_diary_to_kakao_talk)
            ), EXPORT_REQUEST_CODE
        )
    }

    private fun sendAudioToKakaoTalk(activity: Activity, audioUri: String?) {
        if (audioUri == null)
            return

        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, toContentUri(activity, audioUri))
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setPackage("com.kakao.talk")

        activity.startActivityForResult(
            Intent.createChooser(
                shareIntent,
                activity.getString(R.string.send_diary_to_kakao_talk)
            ), EXPORT_REQUEST_CODE
        )
    }

    private fun sendTextToKakaoTalk(activity: Activity, diary: DiaryModel) {
        if (!::weatherWords.isInitialized)
            weatherWords = activity.resources.getStringArray(R.array.weatherWords)

        val stringBuilder = StringBuilder()
        val date = diary.time.toDateFormat(activity.getString(R.string.date_format))
        val time = diary.time.toDateFormat(activity.getString(R.string.time_format))
        val weather = weatherWords[diary.weatherIconIndex]
        val dateTimeWeather = "$date  $time  $weather\n\n"

        stringBuilder.append(dateTimeWeather)
        stringBuilder.append(diary.title + "\n\n")
        stringBuilder.append(diary.content)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setPackage("com.kakao.talk")

        activity.startActivityForResult(
            Intent.createChooser(
                shareIntent,
                activity.getString(R.string.send_diary_to_kakao_talk)
            ), EXPORT_REQUEST_CODE
        )
    }

    fun sendDiaryToFacebook(activity: Activity, mediaUris: List<Pair<Int, Uri>>) {
        if (!::weatherWords.isInitialized)
            weatherWords = activity.resources.getStringArray(R.array.weatherWords)

        val sharePhotos = mutableListOf<SharePhoto>()
        val shareVideos = mutableListOf<ShareVideo>()

        for (media in mediaUris) {
            if (media.first == MediaHelper.MediaType.PHOTO)
                sharePhotos.add(SharePhoto.Builder().setImageUrl(media.second).build())
            else
                shareVideos.add(ShareVideo.Builder().setLocalUrl(media.second).build())
        }

        val shareMediaContentBuilder = ShareMediaContent.Builder()

        if (sharePhotos.isNotEmpty())
            shareMediaContentBuilder.addMedia(sharePhotos.toList())

        if (shareVideos.isNotEmpty())
            shareMediaContentBuilder.addMedia(shareVideos.toList())

        val shareMediaContent = shareMediaContentBuilder.build()
        val shareDialog = ShareDialog(activity)
        shareDialog.show(shareMediaContent, ShareDialog.Mode.AUTOMATIC)
        // shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() { ... });
    }

    fun sendTextToFacebook(activity: Activity, diary: DiaryModel) {
        if (!::weatherWords.isInitialized)
            weatherWords = activity.resources.getStringArray(R.array.weatherWords)

        val stringBuilder = StringBuilder()
        val date = diary.time.toDateFormat(activity.getString(R.string.date_format))
        val time = diary.time.toDateFormat(activity.getString(R.string.time_format))
        val weather = weatherWords[diary.weatherIconIndex]
        val dateTimeWeather = "$date  $time  $weather\n\n"

        stringBuilder.append(dateTimeWeather)
        stringBuilder.append(diary.title + "\n\n")
        stringBuilder.append(diary.content)

        val uri = Uri.parse("R.drawable.ic_moon_24")

        val shareLinkContent = ShareLinkContent.Builder()
            .setContentUrl(uri)
            .setQuote(stringBuilder.toString())
            .setShareHashtag(
                ShareHashtag.Builder()
                    .setHashtag("#ConnectTheWorld #howtoconcat #hahaha")
                    .build()
            )
            .build()

        val shareDialog = ShareDialog(activity)
        shareDialog.show(shareLinkContent, ShareDialog.Mode.AUTOMATIC)
    }

    fun isKakaoTalkInstalled(context: Context): Boolean {
        val applicationInfoList = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (applicationInfo in applicationInfoList)
            if (applicationInfo.packageName == "com.kakao.talk")
                return true

        return false
    }

    fun isFacebookInstalled(context: Context): Boolean {
        val applicationInfoList = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (applicationInfo in applicationInfoList)
            if (applicationInfo.packageName == "com.facebook.katana")
                return true

        return false
    }

    fun showExportTypeDialog(
        context: Context, diary: DiaryModel,
        convertPdfClickListener: () -> Unit,
        shareOnClickListener: () -> Unit,
        sendDiaryToKakaoTalkClickListener: () -> Unit,
        sendDiaryToFacebookClickListener: () -> Unit
    ) {
        val exportTypes = arrayOf(
            Pair(context.getString(R.string.export_text), R.drawable.ic_text_file_24),
            Pair(context.getString(R.string.export_pdf_file), R.drawable.ic_pdf_file_24),
            Pair(context.getString(R.string.share_diary), R.drawable.ic_round_share_24),
            Pair(
                context.getString(R.string.send_diary_to_kakao_talk),
                R.drawable.ic_kakao_talk_150px
            ),
            Pair(context.getString(R.string.send_diary_to_facebook), R.drawable.ic_facebook_24)
        )

        val exportTypeAdapter = object : ArrayAdapter<Pair<String, Int>>(
            context,
            R.layout.item_select_dialog_28,
            R.id.text,
            exportTypes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text = view.text
                val image = view.image

                text.text = exportTypes[position].first
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                image.setImageResource(exportTypes[position].second)

                return view
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.export))
            .setAdapter(exportTypeAdapter) { _, exportType ->
                when(exportType) {
                    EXPORT_AS_TEXT_FILE -> {
                        showInputDialog(
                            context,
                            context.getString(R.string.text_file_name_input_message)
                        ) { fileName ->
                            CoroutineScope(Dispatchers.IO).launch {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                    exportAsTextFileQ(
                                        context,
                                        diary,
                                        fileName
                                    )
                                else
                                    exportAsTextFile(
                                        context,
                                        diary,
                                        fileName
                                    )
                            }
                        }
                    }
                    EXPORT_AS_PDF_FILE -> convertPdfClickListener.invoke()
                    SHARE_DIARY -> shareOnClickListener.invoke()
                    SEND_DIARY_TO_KAKAO_TALK -> {
                        if (isKakaoTalkInstalled(context))
                            sendDiaryToKakaoTalkClickListener.invoke()
                        else
                            showToast(context, context.getString(R.string.kakao_talk_not_found))
                    }
                    SEND_DIARY_TO_FACEBOOK -> {
                        if (isFacebookInstalled(context))
                            sendDiaryToFacebookClickListener.invoke()
                        else
                            showToast(context, context.getString(R.string.facebook_not_found))
                    }
                }
            }
            .show()
    }
}