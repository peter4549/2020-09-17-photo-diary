package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity

object PhotoHelper {

    const val REQUEST_IMAGE_CAPTURE = 1000

    fun dispatchTakePictureIntent(activity: Activity) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }
}