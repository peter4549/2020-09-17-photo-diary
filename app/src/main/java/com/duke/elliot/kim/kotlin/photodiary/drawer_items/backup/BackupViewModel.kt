package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.app.Application
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BackupViewModel(application: Application): ViewModel() {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private var fileUtil: FileUtilities = FileUtilities.getInstance(application)

    fun deleteFiles(paths: List<String>) {
        coroutineScope.launch {
            fileUtil.deleteFiles(paths)
        }
    }
}