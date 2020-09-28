package com.duke.elliot.kim.kotlin.photodiary.diary.media.audio

import android.os.Process

internal abstract class PriorityRunnable : Runnable {
    private var threadPriority: Int

    constructor() {
        threadPriority = Process.THREAD_PRIORITY_BACKGROUND
    }

    @ProcessPriority
    constructor(threadPriority: Int) {
        this.threadPriority = threadPriority
    }

    override fun run() {
        Process.setThreadPriority(threadPriority)
        runImpl()
    }

    protected abstract fun runImpl()
}