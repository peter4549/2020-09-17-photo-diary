package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.R
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PhotoHelper {

    private lateinit var currentPhotoPath: String

    private var currentImageUri: Uri? = null

    fun getCurrentImageUri() = currentImageUri

    fun dispatchImageCaptureIntent(fragment: Fragment) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { imageCaptureIntent ->
            imageCaptureIntent.resolveActivity(fragment.requireContext().packageManager)?.also {
                val imageFile: File? = try {
                    createImageFile(fragment.requireContext())
                } catch (e: IOException) {
                    null
                }

                imageFile?.also {
                    val imageUri = FileProvider.getUriForFile(
                        fragment.requireContext(),
                        "com.duke.elliot.kim.kotlin.photodiary.fileprovider",
                        it
                    )

                    currentImageUri = imageUri

                    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
                    fragment.startActivityForResult(
                        imageCaptureIntent,
                        MediaHelper.REQUEST_IMAGE_CAPTURE
                    )
                }
            }
        }
    }

    fun createImageFile(context: Context, suffix: String = ""): File? {
        try {
            @Suppress("SpellCheckingInspection")
            val timestamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val path = File(context.filesDir, "images")

            if (!path.exists())
                path.mkdirs()

            return File(path, "JPEG_${timestamp}_$suffix.jpg")
        } catch (e: FileNotFoundException) {
            Timber.e(e)
            return null
        } catch (e: IOException) {
            Timber.e(e)
            return null
        }
    }

    fun createTempImageFile(context: Context, fileName: String): File {
        val picturesDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            fileName,
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
            fragment.startActivityForResult(
                Intent.createChooser(
                    imagePickerIntent,
                    fragment.getString(R.string.select_album)
                ), MediaHelper.REQUEST_IMAGE_PICK
            )
        }
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
        try {
            @Suppress("SpellCheckingInspection")
            val timestamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val path = File(context.filesDir, "images")

            if (!path.exists())
                path.mkdirs()

            val outputFile = File(path, "JPEG_$timestamp.jpg")
            val outputStream = FileOutputStream(outputFile)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            return outputFile
        } catch (e: FileNotFoundException) {
            Timber.e(e)
            return null
        } catch (e: IOException) {
            Timber.e(e)
            return null
        }
    }

    fun overrideBitmapToFile(bitmap: Bitmap, imageUri: Uri): File? {
        try {
            imageUri.path?.let { path ->
                val outputFile = File(path)
                val outputStream = FileOutputStream(outputFile)

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                return outputFile
            } ?: run {
                return null
            }
        } catch (e: FileNotFoundException) {
            Timber.e(e)
            return null
        } catch (e: IOException) {
            Timber.e(e)
            return null
        }
    }

    fun bitmapToTempImageFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        val imageFile: File? = try {
            MediaHelper.photoHelper.createTempImageFile(context,
                fileName
            )
        } catch (e: IOException) {
            return null
        }

        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return imageFile
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

fun Bitmap.setConfigure(config: Bitmap.Config): Bitmap = this.copy(config, true)