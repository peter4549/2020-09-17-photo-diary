package com.duke.elliot.kim.kotlin.photodiary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    var action = Action.INITIALIZE
    val diaries = MutableLiveData<ArrayList<String>>()

    init {
        diaries.value = arrayListOf("A", "B", "C")
    }

    fun add(diary: String) {
        action = Action.ADD
        diaries.value = diaries.value?.apply {
            add(diary)
        }

    }

    object Action {
        const val INITIALIZE = 0
        const val INITIALIZED = 1
        const val ADD = 2
        const val REMOVE = 3
    }
}