package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel
import com.duke.elliot.kim.kotlin.photodiary.google_map.PlaceModel
import com.duke.elliot.kim.kotlin.photodiary.utility.getCurrentTime
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class DiaryWritingViewModel(application: Application, val originDiary: DiaryModel?, val mode: Int): ViewModel() {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    var initialized = false

    private val preferences =
        application.getSharedPreferences(PREFERENCES_HASH_TAG, Context.MODE_PRIVATE)

    var textAlignment = Gravity.START
    var textColor = ContextCompat.getColor(application, R.color.colorTextEnabledDark)
    var textFont: Typeface? = null
    var textFontId = MainActivity.DEFAULT_FONT_ID
    var textSize = 18F
    var textStyleBold = false
    var textStyleItalic = false

    var weatherIconIndex = 10

    private val inputHashTag = application.getString(R.string.input_hash_tag)
    val hashTagList: ArrayList<String> = restoreHashTagsFromPreferences()
    var selectedHashTags: ArrayList<String>

    var time: Long = getCurrentTime()

    var title: String = ""
    var content = ""
    var action = Action.UNINITIALIZED

    val mediaArrayList: MutableLiveData<ArrayList<MediaModel>> = if (originDiary != null)
        MutableLiveData(ArrayList(originDiary.mediaArray.toList()))
    else
        MutableLiveData(ArrayList())

    /** Folder */
    lateinit var folderDao: FolderDao
    var folders: MutableList<FolderModel>? = null
    var folder: FolderModel? = null

    init {
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                folderDao = DiaryDatabase.getInstance(application).folderDao()
            }
        }
    }

    /** Place */
    var place: PlaceModel? = originDiary?.place

    private var _mediaArrayListSize = 0

    val mediaArrayListSize: Int
        get() = _mediaArrayListSize

    var selectedItemPosition: Int? = null

    // Fetch data from the original diary.
    init {
        originDiary?.let {
            val textOptions = it.textOptions
            textAlignment = textOptions.textAlignment
            textColor = textOptions.textColor
            textFontId = textOptions.textFontId
            textSize = textOptions.textSize
            textStyleBold = textOptions.textStyleBold
            textStyleItalic = textOptions.textStyleItalic

            weatherIconIndex = it.weatherIconIndex
        }

        textFont = getFont(application, textFontId)

        _mediaArrayListSize = mediaArrayList.value?.size ?: 0

        selectedHashTags = originDiary?.hashTags?.toCollection(ArrayList()) ?: arrayListOf()
    }

    fun addMedia(media: MediaModel) {
        action = Action.ADDED
        mediaArrayList.value = mediaArrayList.value?.apply {
            add(media)
        }
        _mediaArrayListSize = mediaArrayList.value?.count() ?: 0
    }

    fun getCurrentImageUri() = PhotoHelper.getCurrentImageUri()

    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }

    fun storeHashTagToPreferences(hashTag: String) {
        hashTagList.add(hashTag)
        preferences.edit()
            .putStringSet(KEY_HASH_TAG_SET, hashTagList.toSet())
            .apply()
    }

    fun deleteHashTagInPreferences(hashTag: String) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                hashTagList.remove(hashTag)
            }
        }
    }

    fun updateHashTagsToPreferences() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                preferences.edit()
                    .putStringSet(KEY_HASH_TAG_SET, hashTagList.toSet())
                    .apply()
            }
        }
    }

    private fun restoreHashTagsFromPreferences(): ArrayList<String> {
        val arrayList = ArrayList<String>()
        for (hashTag in preferences.getStringSet(KEY_HASH_TAG_SET, setOf(inputHashTag)) ?: setOf(inputHashTag))
            arrayList.add(hashTag)

        arrayList.remove(inputHashTag)
        arrayList.add(0, inputHashTag)

        return arrayList
    }

    companion object {
        val weatherIconIds = arrayOf(
            R.drawable.ic_cloud_24,
            R.drawable.ic_cloudy_24,
            R.drawable.ic_flash_24,
            R.drawable.ic_flash_cloud_24,
            R.drawable.ic_moon_24,
            R.drawable.ic_rain_24,
            R.drawable.ic_rain_cloud_24,
            R.drawable.ic_rainbow_24,
            R.drawable.ic_snow_24,
            R.drawable.ic_snow_cloud_24,
            R.drawable.ic_sun_24
        )

        const val PREFERENCES_HASH_TAG = "preferences_hash_tag"
        const val KEY_HASH_TAG_SET = "key_hash_tag_set"
    }
}