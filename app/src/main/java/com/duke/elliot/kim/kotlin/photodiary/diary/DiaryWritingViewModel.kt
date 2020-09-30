package com.duke.elliot.kim.kotlin.photodiary.diary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper

class DiaryWritingViewModel: ViewModel() {

    val dateTime: String = ""  // TODO be initialized.
    var title: String = ""
    var content = ""
    var action = Action.UNINITIALIZED

    val mediaArrayList = MutableLiveData<ArrayList<MediaModel>>()
    private var _mediaArrayListSize = 0

    val mediaArrayListSize: Int
        get() = _mediaArrayListSize

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

    fun getCurrentPhotoBitmap() = PhotoHelper.getCurrentPhotoBitmap()

    // TODO: ListAdapter를 사용한 업데이트 로직으로 변경 해볼것. 다만 그리드 레이아웃도 가능한지 확인 필요.
    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }
}