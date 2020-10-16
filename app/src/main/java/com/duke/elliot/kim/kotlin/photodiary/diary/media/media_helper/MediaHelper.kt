package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.Intent
import androidx.fragment.app.Fragment
import com.divyanshu.draw.activity.DrawingActivity


object MediaHelper {

    const val REQUEST_IMAGE_CAPTURE = 1000
    const val REQUEST_IMAGE_PICK = 1001
    const val REQUEST_VIDEO_PICK = 1002
    const val REQUEST_AUDIO_PICK = 1003
    const val REQUEST_CODE_DRAW = 1004

    object MediaType {
        const val PHOTO = 0
        const val VIDEO = 1
        const val AUDIO = 2
    }

    val photoHelper = PhotoHelper
    val videoHelper = VideoHelper
    val audioHelper = AudioHelper

    fun startDrawingActivity(fragment: Fragment) {
        val intent = Intent(fragment.requireContext(), DrawingActivity::class.java)
        fragment.startActivityForResult(intent, REQUEST_CODE_DRAW)
    }
}