package com.duke.elliot.kim.kotlin.photodiary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import kotlinx.coroutines.*

class MainViewModel: ViewModel() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    var diariesFragmentAction = Action.UNINITIALIZED
    var photosFragmentAction = Action.UNINITIALIZED

    val diaries = MutableLiveData<ArrayList<DiaryModel>>()

    init {

    }

    fun add(diary: DiaryModel) {
        scope.launch {
            withContext(Dispatchers.IO) {
                if (diariesFragmentAction != Action.UNINITIALIZED)
                    diariesFragmentAction = Action.ADDED

                if (photosFragmentAction != Action.UNINITIALIZED)
                    photosFragmentAction = Action.ADDED

                diaries.value = diaries.value?.apply {
                    add(0, diary)
                }
            }
        }
    }

    object Action {
        const val UNINITIALIZED = 0
        const val INITIALIZED = 1
        const val ADDED = 2
        const val EDITED = 3
        const val REMOVED = 4
    }
}