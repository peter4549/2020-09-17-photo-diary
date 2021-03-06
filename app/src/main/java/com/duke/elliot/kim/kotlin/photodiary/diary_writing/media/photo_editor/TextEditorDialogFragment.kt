package com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.photo_editor

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentTextEditorDialogBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.photo_editor.ColorPickerAdapter.OnColorPickerClickListener
import com.duke.elliot.kim.kotlin.photodiary.utility.ColorUtilities
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.crossFadeIn
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class TextEditorDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentTextEditorDialogBinding
    private var inputMethodManager: InputMethodManager? = null
    private var colorCode = 0
    private var textEditor: TextEditor? = null

    interface TextEditor {
        fun onDone(inputText: String?, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setToFullScreen()
    }

    private fun setToFullScreen() {
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.window?.setLayout(width, height)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    @Suppress("DEPRECATION")
    override fun onStop() {
        super.onStop()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireActivity().window?.setDecorFitsSystemWindows(false)
        } else
            requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_text_editor_dialog,
            container,
            false
        )

        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            binding.lifecycleOwner ?: viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    binding.root.crossFadeIn(320L)
                }
            })

        binding.recyclerViewAddTextColorPicker.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = GridLayoutManager.HORIZONTAL
            }
            adapter = ColorPickerAdapter(requireContext()).apply {
                this.setOnColorPickerClickListener(object : OnColorPickerClickListener {
                    override fun onColorPickerClickListener(colorCode: Int) {
                        this@TextEditorDialogFragment.colorCode = colorCode
                        binding.editTextAddText.setTextColor(colorCode)
                        setCursorColor(binding.editTextAddText, colorCode)
                    }
                })
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        colorCode = arguments?.getInt(EXTRA_COLOR_CODE) ?: android.R.color.white

        inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        binding.editTextAddText.setText(arguments?.getString(EXTRA_INPUT_TEXT))
        binding.editTextAddText.setTextColor(colorCode)
        setCursorColor(binding.editTextAddText, colorCode)
        binding.editTextAddText.requestFocus()

        binding.textDone.setOnClickListener {
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
            dismiss()
            val inputText = binding.editTextAddText.text.toString()
            if (!TextUtils.isEmpty(inputText) && textEditor != null) {
                textEditor?.onDone(inputText, colorCode)
            }
        }
    }

    fun setOnTextEditorListener(textEditor: TextEditor?) {
        this.textEditor = textEditor
    }

    companion object {
        private val TAG: String = TextEditorDialogFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String =
                "",
            @ColorInt colorCode: Int = ContextCompat.getColor(
                appCompatActivity,
                android.R.color.white
            )
        ): TextEditorDialogFragment {
            val bundle = Bundle()
            bundle.putString(EXTRA_INPUT_TEXT, inputText)
            bundle.putInt(EXTRA_COLOR_CODE, colorCode)

            val fragment = TextEditorDialogFragment()
            fragment.arguments = bundle
            fragment.show(appCompatActivity.supportFragmentManager, TAG)

            return fragment
        }
    }

    private fun setCursorColor(editText: EditText, @ColorInt color: Int) {
        val objectColor = ColorUtilities.lightenColor(color, 0.175F)
        var highlightColor = ColorUtilities.lightenColor(ColorUtilities.getComplementaryColor(color), 0.350F)
        highlightColor = ColorUtilities.whiteToGrey(highlightColor)

        ColorUtilities.setCursorDrawableColor(editText, objectColor)
        editText.highlightColor = highlightColor
        ColorUtilities.setCursorPointerColor(editText, objectColor)
    }
}