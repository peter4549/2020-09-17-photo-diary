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

        binding.sendMedia.setOnClickListener {
            onFacebookOptionClickListener.onSendMediaClick()
            dismiss()
        }

        binding.sendText.setOnClickListener {
            onFacebookOptionClickListener.onSendOnlyTextClick()
            dismiss()
        }

        return binding.root
    }
}