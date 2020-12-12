package com.duke.elliot.kim.kotlin.photodiary.utility

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentOkCancelDialogBinding

class OkCancelDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentOkCancelDialogBinding
    private lateinit var cancelButtonOnClick: () -> Unit
    private lateinit var okButtonOnClick: () -> Unit
    private lateinit var title: String
    private lateinit var message: String

    private var cancelButtonText: String? = null
    private var okButtonText: String? = null

    fun setDialogParameters(title: String, message: String, onClick: () -> Unit) {
        this.title = title
        this.message = message
        this.okButtonOnClick = onClick
    }

    fun setCancelClickEvent(onClick: () -> Unit) {
        this.cancelButtonOnClick = onClick
    }

    fun setButtonTexts(okButtonText: String?, cancelButtonText: String?) {
        okButtonText?.let {
            this.okButtonText = it
        }

        cancelButtonText?.let {
            this.cancelButtonText = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_ok_cancel_dialog,
            container,
            false
        )

        binding.textTitle.text = title
        binding.textMessage.text = message

        binding.buttonCancel.setOnClickListener {
            if(::cancelButtonOnClick.isInitialized)
                cancelButtonOnClick.invoke()

            this.dismiss()
        }

        binding.buttonOk.setOnClickListener {
            if (::okButtonOnClick.isInitialized)
                okButtonOnClick.invoke()

            this.dismiss()
        }

        cancelButtonText?.let {
            binding.buttonCancel.text = it
        }

        okButtonText?.let {
            binding.buttonOk.text = it
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return binding.root
    }
}