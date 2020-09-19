package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.content.Intent
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import java.net.URI


object PhotoHelper {

    const val REQUEST_IMAGE_CAPTURE = 1000
    const val REQUEST_IMAGE_PICK = 1001

    fun dispatchImageCaptureIntent(fragment: Fragment) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { imageCaptureIntent ->
            //val photoURI = URI("test") //  Test resolution.
            // imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            imageCaptureIntent.resolveActivity(fragment.requireContext().packageManager)?.also {
                println("AAAAAAAA")
                fragment.startActivityForResult(imageCaptureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun dispatchImagePickerIntent(fragment: Fragment) {
        Intent(Intent.ACTION_PICK).also { imagePickerIntent ->
            imagePickerIntent.type = "image/*"
            imagePickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            fragment.startActivityForResult(imagePickerIntent, REQUEST_IMAGE_PICK)
        }
    }
}