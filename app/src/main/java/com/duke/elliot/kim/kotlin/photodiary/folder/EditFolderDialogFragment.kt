package com.duke.elliot.kim.kotlin.photodiary.folder

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentEditFolderDialogBinding

class EditFolderDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentEditFolderDialogBinding
    private lateinit var onOkButtonClickListener: OnOkButtonClickListener

    interface OnOkButtonClickListener {
        fun onClick()
    }

    fun setOnOkButtonClickListener(onOkButtonClickListener: OnOkButtonClickListener) {
        this.onOkButtonClickListener = onOkButtonClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_folder_dialog, container, false)

        binding.okButton.setOnClickListener {
            onOkButtonClickListener.onClick()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }
}