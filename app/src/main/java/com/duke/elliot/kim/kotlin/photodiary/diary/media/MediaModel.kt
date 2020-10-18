package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaModel(val type: Int,
                      var uriString: String): Parcelable {
    object Type {
        const val PHOTO = 0
        const val VIDEO = 1
        const val AUDIO = 2
    }
}