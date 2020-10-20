package com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.photo_editor

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhotoEditorViewModel: ViewModel() {

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri>
        get() = _imageUri

    fun updateImage(imageUri: Uri) {
        _imageUri.value = imageUri
    }
}