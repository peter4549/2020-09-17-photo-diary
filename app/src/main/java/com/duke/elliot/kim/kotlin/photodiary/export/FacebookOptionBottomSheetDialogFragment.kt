package com.duke.elliot.kim.kotlin.photodiary.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentFacebookOptionBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FacebookOptionBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFacebookOptionBottomSheetDialogBinding
    private lateinit var onFacebookOptionClickListener: OnFacebookOptionClickListener
    // private lateinit var onClickListenerWrapper: OnClickListenerWrapper

    /*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_ON_CLICK_LISTENER_WRAPPER, onClickListenerWrapper)
    }
     */

    interface OnFacebookOptionClickListener {
        fun onSendMediaClick()
        fun onSendOnlyTextClick()
    }

    fun setOnFacebookOptionClickListener(onFacebookOptionClickListener: OnFacebookOptionClickListener) {
        this.onFacebookOptionClickListener = onFacebookOptionClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_facebook_option_bottom_sheet_dialog, container, false)

        /*
        onClickListenerWrapper = if (savedInstanceState != null) {
            savedInstanceState.getSerializable(KEY_ON_CLICK_LISTENER_WRAPPER) as OnClickListenerWrapper
        } else
            OnClickListenerWrapper(onFacebookOptionClickListener)
         */

        binding.sendMedia.setOnClickListener {
            if (::onFacebookOptionClickListener.isInitialized)
                onFacebookOptionClickListener.onSendMediaClick()
            dismiss()
        }

        binding.sendText.setOnClickListener {
            if (::onFacebookOptionClickListener.isInitialized)
                onFacebookOptionClickListener.onSendOnlyTextClick()
            dismiss()
        }

        return binding.root
    }

    /*
    inner class OnClickListenerWrapper(val onClickListener: OnFacebookOptionClickListener) : Serializable

    companion object {
        private const val KEY_ON_CLICK_LISTENER_WRAPPER = "elliot_on_click_listener_wrapper_1850"
    }
     */
}