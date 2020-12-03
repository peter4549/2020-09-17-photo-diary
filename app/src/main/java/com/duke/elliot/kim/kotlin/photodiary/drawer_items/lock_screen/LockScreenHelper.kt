package com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen

import android.content.Context
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.beautycoder.pflockscreen.security.PFSecurityManager
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast

private const val PREFERENCES_LOCK_SCREEN = "imperial_elliot_preferences_lock_screen_20201202"
private const val KEY_ENCODED_CODE = "imperial_elliot_key_encoded_code_20201202"
private const val KEY_LOCK_SCREEN_ON_STATE = "imperial_elliot_key_lock_screen_on_state_20201202"

private const val KEY_SECURITY_QUESTION_INDEX = "imperial_elliot_key_security_question_index_20201203"
private const val KEY_ANSWER = "imperial_elliot_key_answer_index_20201203"

const val LOCK_SCREEN_TAG = "lock_screen_tag"

object LockScreenHelper {
    fun saveSecurityQuestionIndexAnswerPair(context: Context, securityQuestionIndex: Int, answer: String) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_LOCK_SCREEN, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_SECURITY_QUESTION_INDEX, securityQuestionIndex)
        editor.putString(KEY_ANSWER, answer)
        editor.apply()
    }

    fun loadSecurityQuestionIndexAnswerPair(context: Context): Pair<Int, String>? {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_LOCK_SCREEN, Context.MODE_PRIVATE)
        val securityQuestionIndex = sharedPreferences.getInt(KEY_SECURITY_QUESTION_INDEX, -1)
        val answer = sharedPreferences.getString(KEY_ANSWER, "") ?: ""

        if (securityQuestionIndex == -1 || answer.isBlank())
            return null

        return securityQuestionIndex to answer
    }

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

    fun showAuthLockScreen(mainActivity: MainActivity, encodedCode: String) {
        val pfLockScreenFragment = PFLockScreenFragment()
        val builder = PFFLockScreenConfiguration.Builder(mainActivity)
            .setMode(PFFLockScreenConfiguration.MODE_AUTH)
            .setUseFingerprint(true)
            .setTitle(mainActivity.getString(R.string.please_enter_a_password))
            .setLeftButton(mainActivity.getString(R.string.forgot_your_password))
        pfLockScreenFragment.setConfiguration(builder.build())
        pfLockScreenFragment.setEncodedPinCode(encodedCode)
        pfLockScreenFragment.setLoginListener(object :
            PFLockScreenFragment.OnPFLockScreenLoginListener {
            override fun onCodeInputSuccessful() {
                mainActivity.supportFragmentManager.popBackStackImmediate()
            }

            override fun onFingerprintSuccessful() {
                mainActivity.supportFragmentManager.popBackStackImmediate()
            }

            override fun onPinLoginFailed() {
                showToast(mainActivity, mainActivity.getString(R.string.passwords_do_not_match))
            }

            override fun onFingerprintLoginFailed() {
                showToast(mainActivity, mainActivity.getString(R.string.fingerprint_authorization_failed))
            }
        })

        pfLockScreenFragment.setOnLeftButtonClickListener {
            val securityQuestionIndexAnswerPair = LockScreenHelper.loadSecurityQuestionIndexAnswerPair(it.context)
            val correctSecurityQuestionIndex = securityQuestionIndexAnswerPair?.first ?: -1
            val correctAnswer = securityQuestionIndexAnswerPair?.second

            if (correctSecurityQuestionIndex == -1 || correctAnswer.isNullOrBlank())
                showToast(it.context, mainActivity.getString(R.string.security_question_is_not_set))
            else {
                val securityQuestionDialogFragment = SecurityQuestionsDialogFragment().apply {
                    setTitle(mainActivity.getString(R.string.forgot_your_password))
                    setMode(AUTHORIZE_SECURITY_QUESTION)
                    setContainerViewId(R.id.nav_host_fragment)
                }
                securityQuestionDialogFragment.show(
                    mainActivity.supportFragmentManager,
                    securityQuestionDialogFragment.tag
                )
            }
        }

        mainActivity.supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out)
            .replace(R.id.nav_host_fragment, pfLockScreenFragment, LOCK_SCREEN_TAG)
            .commit()
    }
}