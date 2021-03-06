package com.duke.elliot.kim.kotlin.photodiary.folder

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentEditFolderDialogBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.toHexColor
import kotlinx.android.synthetic.main.fragment_edit_folder_dialog.*
import kotlinx.coroutines.*
import petrov.kristiyan.colorpicker.ColorPicker

class EditFolderDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentEditFolderDialogBinding
    private lateinit var folder: FolderModel
    private lateinit var folderDao: FolderDao
    private var folderColor = 0
    private var mode = -1

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var callbackAfterEditing: (FolderModel) -> Unit
    fun setCallbackAfterEditing(callbackAfterEditing: (FolderModel) -> Unit) {
        this.callbackAfterEditing = callbackAfterEditing
    }

    fun setMode(mode: Int) {
        this.mode = mode
    }

    fun setFolder(folder: FolderModel) {
        this.folder = folder
    }

    fun setFolderDao(folderDao: FolderDao) {
        this.folderDao = folderDao
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_folder_dialog, container, false)
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        folderColor = ContextCompat.getColor(requireContext(), R.color.colorRed200)  // Default color

        binding.title.text = when(mode) {
            ADD_MODE -> getString(R.string.add_folder)
            else -> getString(R.string.edit_folder)
        }

        if (mode == EDIT_MODE)
            binding.folderName.setText(folder.name)

        binding.folderColor.setCardBackgroundColor(folderColor)
        binding.okButton.setOnClickListener {
            when(mode) {
                ADD_MODE -> createFolder()?.let {
                    insertFolder(it)
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                } ?: run {
                    folderNameContainer.isErrorEnabled = true
                    folderNameContainer.error = getString(R.string.enter_the_folder_name)
                }
                EDIT_MODE -> {
                    if (binding.folderName.text.toString().isNotBlank()) {
                        folder.let {
                            it.name = binding.folderName.text.toString()
                            it.color = folderColor

                            updateFolder(it)
                            callbackAfterEditing.invoke(it)
                        }


                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                    } else {
                        folderNameContainer.isErrorEnabled = true
                        folderNameContainer.error = getString(R.string.enter_the_folder_name)
                    }
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.selectFolderColorContainer.setOnClickListener {
            val colorPicker = ColorPicker(requireActivity())
            val themeColors = requireContext().resources.getIntArray(R.array.theme_colors).toList()
            val hexColors = themeColors.map { it.toHexColor() } as ArrayList

            colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    folderColor = color
                    binding.folderColor.setCardBackgroundColor(folderColor)
                }

                override fun onCancel() {  }
            })
                .setTitle(getString(R.string.select_folder_color))
                .setColumns(6)
                .setColorButtonMargin(2, 2, 2, 2)
                .setColorButtonDrawable(R.drawable.background_white_rounded_corners)
                .setColors(hexColors)
                .setDefaultColorButton(folderColor)
                .show()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    private fun insertFolder(folder: FolderModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                folderDao.insert(folder)
            }

            dismiss()
        }
    }

    private fun updateFolder(folder: FolderModel) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                folderDao.update(folder)
            }

            dismiss()
        }
    }

    private fun createFolder(): FolderModel? {
        val name = binding.folderName.text.toString()

        if (name.isBlank())
            return null

        return FolderModel(
            name = name,
            color = folderColor
        )
    }

    companion object {
        const val ADD_MODE = 0
        const val EDIT_MODE = 1
    }
}