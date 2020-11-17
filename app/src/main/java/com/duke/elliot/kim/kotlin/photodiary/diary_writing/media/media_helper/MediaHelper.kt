package com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper

import android.content.Intent
import androidx.fragment.app.Fragment
import com.divyanshu.draw.activity.DrawingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


object MediaHelper {

    const val REQUEST_IMAGE_CAPTURE = 1000
    const val REQUEST_IMAGE_PICK = 1001
    const val REQUEST_VIDEO_PICK = 1002
    const val REQUEST_AUDIO_PICK = 1003
    const val REQUEST_CODE_DRAW = 1004

    object MediaType {
        const val NULL = -1
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

    fun deleteFile(path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(path)
            if (file.exists()) {
                if (file.delete())
                    Timber.d("File deleted: ${file.name}")
                else
                    Timber.e("Failed to delete file: ${file.name}")
            } else {
                Timber.e("File not found: ${file.name}")
            }
        }
    }
}