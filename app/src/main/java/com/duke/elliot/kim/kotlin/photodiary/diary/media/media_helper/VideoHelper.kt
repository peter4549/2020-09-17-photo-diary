package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.Intent
import androidx.fragment.app.Fragment

object VideoHelper {

    fun dispatchVideoPickerIntent(fragment: Fragment) {
        Intent(Intent.ACTION_PICK).also { videoPickerIntent ->
            videoPickerIntent.type = "video/*"
            videoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            fragment.startActivityForResult(videoPickerIntent, MediaHelper.REQUEST_VIDEO_PICK)
        }
    }
}