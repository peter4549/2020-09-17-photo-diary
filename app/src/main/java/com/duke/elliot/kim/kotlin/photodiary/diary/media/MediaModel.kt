package com.duke.elliot.kim.kotlin.photodiary.diary.media

data class MediaModel(val type: Int,
                      var uriString: String) {
    object Type {
        const val PHOTO = 0
        const val VIDEO = 1
        const val AUDIO = 2
    }
}