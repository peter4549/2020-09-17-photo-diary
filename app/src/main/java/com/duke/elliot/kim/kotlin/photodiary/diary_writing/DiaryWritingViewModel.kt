package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.app.Application
import android.graphics.Typeface
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.getCurrentTime
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont
import java.util.*
import kotlin.collections.ArrayList

class DiaryWritingViewModel(application: Application, val originDiary: DiaryModel?, val mode: Int): ViewModel() {

    // 얘가 기존의 데이터를 받아서 불러오는 방식. 즉 아래의 데이터들은, diary data class로 부터 파싱될 예정.
    // private
    var initialized = false

    var textAlignment = Gravity.START
    var textColor = ContextCompat.getColor(application, R.color.colorTextEnabledDark)
    var textFont: Typeface? = null
    var textFontId = MainActivity.DEFAULT_FONT_ID
    var textSize = 18F
    var textStyleBold = false
    var textStyleItalic = false

    var weatherIconId = R.drawable.ic_sun_24

    private val _time: Long = getCurrentTime()
    val time: Long
        get() = _time

    var title: String = ""
    var content = ""
    var action = Action.UNINITIALIZED

    val mediaArrayList: MutableLiveData<ArrayList<MediaModel>> = if (originDiary != null)
        MutableLiveData(ArrayList(originDiary.mediaArray.toList()))
    else
        MutableLiveData(ArrayList())

    private var _mediaArrayListSize = 0

    val mediaArrayListSize: Int
        get() = _mediaArrayListSize

    var selectedItemPosition: Int? = null

    init {
        originDiary?.let {
            val textOptions = it.textOptions
            textAlignment = textOptions.textAlignment
            textColor = textOptions.textColor
            textFontId = textOptions.textFontId
            textSize = textOptions.textSize
            textStyleBold = textOptions.textStyleBold
            textStyleItalic = textOptions.textStyleItalic
        }

        textFont = getFont(application, textFontId)

        _mediaArrayListSize = mediaArrayList.value?.size ?: 0
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
    }
}