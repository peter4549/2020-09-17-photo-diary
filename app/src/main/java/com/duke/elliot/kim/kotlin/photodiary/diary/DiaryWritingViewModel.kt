package com.duke.elliot.kim.kotlin.photodiary.diary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel

class DiaryWritingViewModel: ViewModel() {
    val dateTime: String = ""  // TODO be initialized.
    var title: String = ""
    var content = ""

    val mediaArrayList = MutableLiveData<ArrayList<MediaModel>>()
}