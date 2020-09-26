package com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper

import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryWritingFragmentDirections

object AudioHelper {
    fun dispatchAudioPickerIntent(fragment: Fragment) {
        Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            .also { audioPickerIntent ->
                audioPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                fragment.startActivityForResult(audioPickerIntent, MediaHelper.REQUEST_AUDIO_PICK)
            }
    }

    fun startAudioRecordingFragment(view: View) {
        view.findNavController().navigate(DiaryWritingFragmentDirections.actionDiaryWritingFragmentToAudioRecordingFragment())
    }
}