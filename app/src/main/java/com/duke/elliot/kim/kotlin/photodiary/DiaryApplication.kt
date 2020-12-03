package com.duke.elliot.kim.kotlin.photodiary

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.getNightMode
import com.duke.elliot.kim.kotlin.photodiary.utility.TypefaceUtil
import com.facebook.FacebookSdk
import timber.log.Timber

class DiaryApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppCompatDelegate.setDefaultNightMode(getNightMode(this))
    }
}