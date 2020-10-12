package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.graphics.Bitmap
import android.net.Uri

data class MediaModel(val type: Int,
                      var uri: Uri) {
    object Type {
        const val PHOTO = 0
        const val VIDEO = 1
        const val AUDIO = 2
    }
}