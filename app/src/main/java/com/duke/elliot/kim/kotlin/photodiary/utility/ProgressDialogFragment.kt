package com.duke.elliot.kim.kotlin.photodiary.utility

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.photodiary.R

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_progress_dialog, container, false)
        val progressBar = view.findViewById(R.id.progress_bar) as ProgressBar
        progressBar.indeterminateDrawable
            .setColorFilter(Color.TRANSPARENT)

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keyCode, _ ->
            when (keyCode) {
                KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_SEARCH -> true
                else -> false
            }
        }
        return dialog
    }

    companion object {
        val instance: ProgressDialogFragment
            get() {
                val fragment = ProgressDialogFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                return fragment
            }
    }
}