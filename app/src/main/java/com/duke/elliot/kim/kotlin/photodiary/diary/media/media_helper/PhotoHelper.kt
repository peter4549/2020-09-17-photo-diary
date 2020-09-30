package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PhotoHelper {

    private lateinit var currentPhotoPath: String

    fun dispatchImageCaptureIntent(fragment: Fragment) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { imageCaptureIntent ->
            imageCaptureIntent.resolveActivity(fragment.requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile(fragment.requireContext())
                } catch (ex: IOException) {
                    null
                }

                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        fragment.requireContext(),
                        "com.duke.elliot.kim.kotlin.photodiary.fileprovider",
                        it
                    )
                    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    fragment.startActivityForResult(imageCaptureIntent, MediaHelper.REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun createImageFile(context: Context): File {
        val timestamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val picturesDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            picturesDirectory
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun getCurrentPhotoBitmap(): Bitmap? {
        val photoFile = File(currentPhotoPath)
        return if (photoFile.exists())
            BitmapFactory.decodeFile(photoFile.absolutePath).rotate(currentPhotoPath)
        else
            null
    }

    fun dispatchImagePickerIntent(fragment: Fragment, getContent: Boolean) {
        Intent(Intent.ACTION_PICK).also { imagePickerIntent ->
            if (getContent)
                imagePickerIntent.action = Intent.ACTION_GET_CONTENT
            imagePickerIntent.type = "image/*"
            imagePickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            fragment.startActivityForResult(Intent.createChooser(imagePickerIntent,
                fragment.getString(R.string.select_album)), MediaHelper.REQUEST_IMAGE_PICK)
        }
    }
}

fun Bitmap.rotate(uri: String): Bitmap {
    try {
        val exif = ExifInterface(uri)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        val matrix = Matrix()

        when(orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1F, 1F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.apply {
                setRotate(180F)
                postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> matrix.apply {
                setRotate(90F)
                postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
            ExifInterface.ORIENTATION_TRANSVERSE -> matrix.apply {
                setRotate(-90F)
                postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90F)
            else -> return this
        }

        return Bitmap.createBitmap(
            this,
            0,
            0,
            this.width,
            this.height,
            matrix,
            true
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return this
    }
}