package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.R
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object VideoHelper {

    fun dispatchVideoPickerIntent(fragment: Fragment, getContent: Boolean) {
        Intent(Intent.ACTION_PICK).also { videoPickerIntent ->
            if (getContent)
                videoPickerIntent.action = Intent.ACTION_GET_CONTENT
            videoPickerIntent.type = "video/*"
            videoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            fragment.startActivityForResult(
                Intent.createChooser(videoPickerIntent, fragment.getString(
                R.string.select_album)), MediaHelper.REQUEST_VIDEO_PICK
            )
        }
    }
}