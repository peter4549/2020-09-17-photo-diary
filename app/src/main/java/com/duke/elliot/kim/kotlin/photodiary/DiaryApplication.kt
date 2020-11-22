package com.duke.elliot.kim.kotlin.photodiary

import android.app.Application
import com.facebook.FacebookSdk
import timber.log.Timber

class DiaryApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        Timber.plant(Timber.DebugTree())
    }
}