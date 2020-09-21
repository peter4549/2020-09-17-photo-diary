package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.graphics.Bitmap
import android.net.Uri

data class MediaModel(val type: Int,
                      val bitmap: Bitmap? = null,
                      val videoUri: Uri? = null) {
    object Type {
        const val PHOTO = 0
        const val VIDEO = 1
        const val VOICE_RECORDING = 2
        const val MUSIC = 3
    }
}