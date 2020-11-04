package com.duke.elliot.kim.kotlin.photodiary.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel


class PdfPreviewViewModelFactory(private val diary: DiaryModel): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(PdfPreviewViewModel::class.java)) {
            return PdfPreviewViewModel(diary) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}