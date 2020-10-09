package com.duke.elliot.kim.kotlin.photodiary.diary

import android.content.Context
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor.PhotoEditorFragment
import com.duke.elliot.kim.kotlin.photodiary.diary.media.simple_crop_view.SimpleCropViewFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.getCurrentDateString
import com.duke.elliot.kim.kotlin.photodiary.utility.getCurrentTimeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class DiaryWritingViewModel: ViewModel() {

    // 얘가 기존의 데이터를 받아서 불러오는 방식. 즉 아래의 데이터들은, diary data class로 부터 파싱될 예정.

    private val _date: String = getCurrentDateString()
    val date: String
        get() = _date

    private val _time: String = getCurrentTimeString()
    val time: String
        get() = _time

    var title: String = ""
    var content = ""
    var action = Action.UNINITIALIZED

    val mediaArrayList = MutableLiveData<ArrayList<MediaModel>>()
    private var _mediaArrayListSize = 0

    val mediaArrayListSize: Int
        get() = _mediaArrayListSize

    var selectedItemPosition: Int? = null

    init {
        mediaArrayList.value = arrayListOf()
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

    fun deleteTempJpegFiles(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val tempJpegFileNames = arrayOf(
                PhotoEditorFragment.PHOTO_EDITOR_IMAGE_FILE_NAME,
                SimpleCropViewFragment.SIMPLE_CROP_VIEW_IMAGE_FILE_NAME
            )
            val picturesDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val files = picturesDirectory?.listFiles()?.filter {
                it.name.endsWith(".jpg") && (
                        it.name.startsWith(tempJpegFileNames[0])
                                || it.name.startsWith(tempJpegFileNames[1])
                        )
            }?.filterNotNull()

            if (files != null) {
                for (file in files) {
                    if (file.exists()) {
                        if (file.delete())
                            Timber.d("File deleted: ${file.name}")
                        else
                            Timber.e("Failed to delete file: ${file.name}")
                    }
                }
            }
        }
    }

    // TODO: ListAdapter를 사용한 업데이트 로직으로 변경 해볼것. 다만 그리드 레이아웃도 가능한지 확인 필요. 되는것으로 확인!
    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }
}