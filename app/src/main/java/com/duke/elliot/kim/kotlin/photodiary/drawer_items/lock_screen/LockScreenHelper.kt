package com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen

import android.content.Context
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.beautycoder.pflockscreen.security.PFSecurityManager
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast

const val PREFERENCES_LOCK_SCREEN = "imperial_elliot_preferences_lock_screen_20201202"
const val KEY_ENCODED_CODE = "imperial_elliot_key_encoded_code_20201202"
const val KEY_LOCK_SCREEN_ON_STATE = "imperial_elliot_key_lock_screen_on_state_20201202"

object LockScreenHelper {
    fun encodedCodeExist(context: Context): Boolean {
        return loadEncodedPinCode(context).isNotBlank()
    }

    fun saveLockScreenOnState(context: Context, on: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_LOCK_SCREEN, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_LOCK_SCREEN_ON_STATE, on)
        editor.apply()
    }

    fun loadLockScreenOnState(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_LOCK_SCREEN, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_LOCK_SCREEN_ON_STATE, false)
    }

    fun recoveryEmailRegistered(): Boolean {
        return true
    }

    fun saveEncodedPinCode(context: Context, encodedCode: String) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_LOCK_SCREEN, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_ENCODED_CODE, encodedCode)
        editor.apply()
    }

    fun loadEncodedPinCode(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_LOCK_SCREEN, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_ENCODED_CODE, "") ?: ""
    }

    fun showAuthLockScreen(mainActivity: MainActivity) {
        val pfLockScreenFragment = PFLockScreenFragment()
        val builder = PFFLockScreenConfiguration.Builder(mainActivity)
            .setMode(PFFLockScreenConfiguration.MODE_AUTH)
            .setUseFingerprint(true)
        pfLockScreenFragment.setConfiguration(builder.build())
        pfLockScreenFragment.setEncodedPinCode(loadEncodedPinCode(mainActivity))
        pfLockScreenFragment.setLoginListener(object :
            PFLockScreenFragment.OnPFLockScreenLoginListener {
            override fun onCodeInputSuccessful() {
                showToast(mainActivity, "코드성공 ")
            }

            override fun onFingerprintSuccessful() {
                showToast(mainActivity, "지문로긴성")
            }

            override fun onPinLoginFailed() {
                showToast(mainActivity, "핀로긴실패")
            }

            override fun onFingerprintLoginFailed() {
                showToast(mainActivity, "지문로긴실패. ")
            }
        })

        pfLockScreenFragment.setOnLeftButtonClickListener {
            // TODO 비번 찾기.
        }

        mainActivity.supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
            .replace(R.id.nav_host_fragment, pfLockScreenFragment)
            .commit()

    }


}