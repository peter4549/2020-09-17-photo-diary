package com.duke.elliot.kim.kotlin.photodiary.utility

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.photodiary.R
import kotlinx.android.synthetic.main.fragment_ok_cancel_dialog.view.*

class OkCancelDialogFragment(): DialogFragment() {

    private lateinit var okButtonOnClick: () -> Unit
    private lateinit var title: String
    private lateinit var message: String

    fun setDialogParameters(title: String, message: String, onClick: () -> Unit) {
        this.title = title
        this.message = message
        this.okButtonOnClick = onClick
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_ok_cancel_dialog, null)

        view.text_title.text = title
        view.text_message.text = message
        view.button_cancel.setOnClickListener {
            this.dismiss()
        }

        view.button_ok.setOnClickListener {
            if (::okButtonOnClick.isInitialized)
                okButtonOnClick.invoke()
        }

        builder.setView(view)
        return builder.create()
    }
}