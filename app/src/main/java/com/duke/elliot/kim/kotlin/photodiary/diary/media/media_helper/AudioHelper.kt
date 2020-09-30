package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.Intent
import androidx.fragment.app.Fragment

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
}