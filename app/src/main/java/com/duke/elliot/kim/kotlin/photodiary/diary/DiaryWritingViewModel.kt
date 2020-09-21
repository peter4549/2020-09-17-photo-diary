package com.duke.elliot.kim.kotlin.photodiary.diary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.VideoHelper

class DiaryWritingViewModel: ViewModel() {

    private lateinit var fragment: DiaryWritingFragment

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

    fun setFragment(fragment: DiaryWritingFragment) {
        this.fragment = fragment
    }

    fun addMedia(media: MediaModel) {
        action = Action.ADDED
        mediaArrayList.value = mediaArrayList.value?.apply {
            add(media)
        }
        _mediaArrayListSize = mediaArrayList.value?.count() ?: 0
    }

    fun runCamera() {
        MediaHelper.photoHelper.dispatchImageCaptureIntent(fragment)
    }

    fun openPhotoGallery() {
        MediaHelper.photoHelper.dispatchImagePickerIntent(fragment)
    }

    fun launchGooglePhotosForPhoto() {
        MediaHelper.launchGooglePhotosPicker(fragment, MediaHelper.MediaType.PHOTO)
    }

    fun getCurrentPhotoBitmap() = PhotoHelper.getCurrentPhotoBitmap()

    fun openVideoGallery() {
        MediaHelper.videoHelper.dispatchVideoPickerIntent(fragment)
    }

    fun launchGooglePhotosForVideo() {
        MediaHelper.launchGooglePhotosPicker(fragment, MediaHelper.MediaType.VIDEO)
    }

    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }
}