package com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment.OnPFLockScreenCodeCreateListener
import com.beautycoder.pflockscreen.security.PFSecurityManager
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.ActivitySetLockScreenBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import timber.log.Timber

class SetLockScreenActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySetLockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_lock_screen)

        binding.switchSetLockScreen.isChecked = LockScreenHelper.loadLockScreenOnState(this)

        binding.setLockScreen.setOnClickListener {
            if (!binding.switchSetLockScreen.isChecked) {
                if (LockScreenHelper.encodedCodeExist(this)) {
                    binding.switchSetLockScreen.toggle()
                    LockScreenHelper.saveLockScreenOnState(this, true)
                } else {
                    showCreateCodeLockScreen()
                }
            } else {
                binding.switchSetLockScreen.toggle()
                LockScreenHelper.saveLockScreenOnState(this, false)
            }
        }

        binding.changePassword.setOnClickListener {
            if (LockScreenHelper.encodedCodeExist(this))
                showChangeCodeLockScreen()
            else
                showToast(this, getString(R.string.no_password_set))
        }

        binding.initializePassword.setOnClickListener {
            if (LockScreenHelper.encodedCodeExist(this))
                showInitializePasswordLockScreen()
            else
                showToast(this, getString(R.string.no_password_set))
        }
    }

    private fun showCreateCodeLockScreen(mode: Int = CREATE_MODE) {
        val title =
            if (mode == CREATE_MODE)
                getString(R.string.please_enter_a_password)
            else
                getString(R.string.please_enter_a_new_password)

        val enterAnimResId =
            if (mode == CREATE_MODE)
                R.anim.anim_slide_in_from_bottom
            else
                0

        val completeMessage =
            if (mode == CREATE_MODE)
                getString(R.string.lock_screen_set)
            else
                getString(R.string.password_changed)

        val pfLockScreenFragment = PFLockScreenFragment()
        val builder = PFFLockScreenConfiguration.Builder(this)
            .setMode(PFFLockScreenConfiguration.MODE_CREATE)
            .setUseFingerprint(true)
            .setTitle(title)
            .setNextButton(" ")

        pfLockScreenFragment.setConfiguration(builder.build())
        pfLockScreenFragment.setCodeCreateListener(object : OnPFLockScreenCodeCreateListener {
            override fun onCodeCreated(encodedCode: String) {
                binding.switchSetLockScreen.toggle()
                LockScreenHelper.saveEncodedPinCode(this@SetLockScreenActivity, encodedCode)
                LockScreenHelper.saveLockScreenOnState(this@SetLockScreenActivity, true)
                showToast(this@SetLockScreenActivity, completeMessage)
                supportFragmentManager.popBackStackImmediate()
            }

            override fun onNewCodeValidationFailed() {  }
        })

        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                enterAnimResId,
                R.anim.anim_slide_out_to_top,
                R.anim.anim_slide_in_from_bottom,
                R.anim.anim_slide_out_to_bottom)
            .replace(R.id.activity_set_lock_screen_container, pfLockScreenFragment)
            .commit()
    }

    private fun showChangeCodeLockScreen() {
        val pfLockScreenFragment = PFLockScreenFragment()
        val builder = PFFLockScreenConfiguration.Builder(this)
            .setMode(PFFLockScreenConfiguration.MODE_AUTH)
            .setUseFingerprint(true)
            .setTitle(getString(R.string.please_enter_your_existing_password))
            .setLeftButton(getString(R.string.forgot_your_password))
        pfLockScreenFragment.setConfiguration(builder.build())
        pfLockScreenFragment.setEncodedPinCode(LockScreenHelper.loadEncodedPinCode(this))
        pfLockScreenFragment.setLoginListener(object : PFLockScreenFragment.OnPFLockScreenLoginListener {
            override fun onCodeInputSuccessful() {
                supportFragmentManager.popBackStackImmediate()
                showCreateCodeLockScreen(CHANGE_MODE)
            }

            override fun onFingerprintSuccessful() {
                supportFragmentManager.popBackStackImmediate()
                showCreateCodeLockScreen(CHANGE_MODE)
            }

            override fun onPinLoginFailed() {
                showToast(this@SetLockScreenActivity, getString(R.string.passwords_do_not_match))
            }

            override fun onFingerprintLoginFailed() {
                showToast(this@SetLockScreenActivity, getString(R.string.fingerprint_authorization_failed))
            }
        })

        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.anim_slide_in_from_bottom,
                R.anim.anim_slide_out_to_top,
                R.anim.anim_slide_in_from_bottom,
                R.anim.fade_out)
            .replace(R.id.activity_set_lock_screen_container, pfLockScreenFragment)
            .commit()
    }

    private fun showInitializePasswordLockScreen() {
        val pfLockScreenFragment = PFLockScreenFragment()
        val builder = PFFLockScreenConfiguration.Builder(this)
            .setMode(PFFLockScreenConfiguration.MODE_AUTH)
            .setUseFingerprint(true)
            .setTitle(getString(R.string.please_enter_your_existing_password))
            .setLeftButton(getString(R.string.forgot_your_password))
        pfLockScreenFragment.setConfiguration(builder.build())
        pfLockScreenFragment.setEncodedPinCode(LockScreenHelper.loadEncodedPinCode(this))
        pfLockScreenFragment.setLoginListener(object : PFLockScreenFragment.OnPFLockScreenLoginListener {
            override fun onCodeInputSuccessful() {
                PFSecurityManager.getInstance().pinCodeHelper.delete{
                    Timber.d("Pin code deleted.")
                }

                binding.switchSetLockScreen.isChecked = false
                LockScreenHelper.saveLockScreenOnState(this@SetLockScreenActivity, false)
                LockScreenHelper.saveEncodedPinCode(this@SetLockScreenActivity, "")
                showToast(this@SetLockScreenActivity, getString(R.string.lock_screen_password_initialized))
                supportFragmentManager.popBackStackImmediate()
            }

            override fun onFingerprintSuccessful() {
                PFSecurityManager.getInstance().pinCodeHelper.delete{
                    Timber.d("Pin code deleted.")
                }

                binding.switchSetLockScreen.isChecked = false
                LockScreenHelper.saveLockScreenOnState(this@SetLockScreenActivity, false)
                LockScreenHelper.saveEncodedPinCode(this@SetLockScreenActivity, "")
                showToast(this@SetLockScreenActivity, getString(R.string.lock_screen_password_initialized))
                supportFragmentManager.popBackStackImmediate()
            }

            override fun onPinLoginFailed() {
                showToast(this@SetLockScreenActivity, getString(R.string.passwords_do_not_match))
            }

            override fun onFingerprintLoginFailed() {
                showToast(this@SetLockScreenActivity, getString(R.string.fingerprint_authorization_failed))
            }
        })

        pfLockScreenFragment.setOnLeftButtonClickListener {
            // TODO 비번 찾기.
            println("aaaaa")
        }

        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.anim_slide_in_from_bottom,
                R.anim.anim_slide_out_to_top,
                R.anim.anim_slide_in_from_bottom,
                R.anim.anim_slide_out_to_bottom
            )
            .replace(R.id.activity_set_lock_screen_container, pfLockScreenFragment)
            .commit()

    }

    companion object {
        private const val CREATE_MODE = 0
        private const val CHANGE_MODE = 1
    }
}
