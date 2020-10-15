package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.R


object AudioHelper {
    fun dispatchAudioPickerIntent(fragment: Fragment, getContent: Boolean) {
        Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            .also { audioPickerIntent ->
                if (getContent)
                    audioPickerIntent.action = Intent.ACTION_GET_CONTENT
                audioPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                fragment.startActivityForResult(audioPickerIntent, MediaHelper.REQUEST_AUDIO_PICK)
            }
    }

    fun dispatchAudioContentPickerIntent(fragment: Fragment) {
        Intent(Intent.ACTION_PICK).also { audioPickerIntent ->
            audioPickerIntent.action = Intent.ACTION_GET_CONTENT
            audioPickerIntent.type = "audio/*"
            audioPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            fragment.startActivityForResult(
                Intent.createChooser(
                    audioPickerIntent, fragment.getString(
                        R.string.select_album
                    )
                ), MediaHelper.REQUEST_AUDIO_PICK
            )
        }
    }

    @SuppressLint("Recycle")
    fun getAudioThumbnail(context: Context, uri: Uri) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        val byteArray = mediaMetadataRetriever.embeddedPicture
        val art: Bitmap
        val bitmapFactoryOptions = BitmapFactory.Options()

        mediaMetadataRetriever.setDataSource(context, uri)


        if (null != byteArray) art = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, bitmapFactoryOptions)

// Code that uses the cover art retrieved below.


    }
}