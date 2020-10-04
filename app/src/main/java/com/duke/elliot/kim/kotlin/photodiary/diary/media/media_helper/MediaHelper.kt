package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import com.divyanshu.draw.activity.DrawingActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.showToast


object MediaHelper {

    const val REQUEST_IMAGE_CAPTURE = 1000
    const val REQUEST_IMAGE_PICK = 1001
    const val REQUEST_VIDEO_PICK = 1002
    const val REQUEST_AUDIO_PICK = 1003
    const val REQUEST_CODE_DRAW = 1004
    private const val GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"

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