package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.showToast


object MediaHelper {

    const val REQUEST_IMAGE_CAPTURE = 1000
    const val REQUEST_IMAGE_PICK = 1001
    const val REQUEST_VIDEO_PICK = 1002
    const val REQUEST_RECORD_AUDIO = 1003
    const val REQUEST_AUDIO_PICK = 1004
    private const val GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"

    object MediaType {
        const val PHOTO = 0
        const val VIDEO = 1
        const val VOICE_RECORDING = 2
        const val MUSIC = 3
    }

    val photoHelper = PhotoHelper
    val videoHelper = VideoHelper
    val audioHelper = AudioHelper

    private fun isGooglePhotosInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(
                GOOGLE_PHOTOS_PACKAGE_NAME,
                PackageManager.GET_ACTIVITIES
            ) != null
        } catch (e: PackageManager.NameNotFoundException) {
            showToast(context, context.getString(R.string.google_photos_not_found))
            false
        }
    }

    fun launchGooglePhotosPicker(fragment: Fragment, type: Int) {
        if (isGooglePhotosInstalled(fragment.requireContext())) {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type =
                if(type == MediaType.PHOTO)
                    "image/*"
                else
                    "video/*"

            val resolveInfoList =
                fragment.requireContext().packageManager.queryIntentActivities(intent, 0)

            resolveInfoList.filter { it.activityInfo?.packageName == GOOGLE_PHOTOS_PACKAGE_NAME }.apply {
                if (this.isNotEmpty()) {
                    val googlePhotosInfo = this[0]
                    intent.component =
                        ComponentName(
                            googlePhotosInfo.activityInfo.packageName,
                            googlePhotosInfo.activityInfo.name
                        )
                    fragment.startActivityForResult(
                        intent,
                        REQUEST_IMAGE_PICK
                    )
                } else {
                    showToast(
                        fragment.requireContext(),
                        fragment.requireContext().getString(R.string.google_photos_not_found)
                    )
                }
            }
        }
    }
}