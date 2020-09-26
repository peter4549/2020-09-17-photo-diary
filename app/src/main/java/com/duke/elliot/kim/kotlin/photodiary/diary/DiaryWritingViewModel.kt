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
        synchronized(this) {
            MediaHelper.photoHelper.dispatchImageCaptureIntent(fragment)
        }
    }

    fun openPhotoGallery() {
        synchronized(this) {
            MediaHelper.photoHelper.dispatchImagePickerIntent(fragment)
        }
    }

    // TODO 더 많은 사진 보기. get_content 함수로 대체되어야함.
    fun launchGooglePhotosForPhoto() {
        synchronized(this) {
            MediaHelper.launchGooglePhotosPicker(fragment, MediaHelper.MediaType.PHOTO)
        }
    }

    fun getCurrentPhotoBitmap() = PhotoHelper.getCurrentPhotoBitmap()

    fun openVideoGallery() {
        synchronized(this) {
            MediaHelper.videoHelper.dispatchVideoPickerIntent(fragment)
        }
    }

    // TODO 더 많은 사진 보기. get_content 함수로 대체되어야함.
    fun launchGooglePhotosForVideo() {
        MediaHelper.launchGooglePhotosPicker(fragment, MediaHelper.MediaType.VIDEO)
    }

    fun openAudioGallery() {
        synchronized(this) {
            MediaHelper.audioHelper.dispatchAudioPickerIntent(fragment)
        }
    }

    fun startAudioRecordingFragment() {
        fragment.view?.let { MediaHelper.audioHelper.startAudioRecordingFragment(it) }
    }

    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }
}