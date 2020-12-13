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
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.databinding.*
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.crossFadeIn
import com.duke.elliot.kim.kotlin.photodiary.utility.crossFadeOut

class SelectFolderDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentSelectFolderDialogBinding
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var folderDao: FolderDao
    private lateinit var onFolderClickListener: (FolderModel) -> Unit
    private var initialized = false

    fun setOnFolderClickListener(onFolderClickListener: (FolderModel) -> Unit) {
        this.onFolderClickListener = onFolderClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_folder_dialog, container, false)

        val database = DiaryDatabase.getInstance(requireContext())
        folderDao = database.folderDao()

        binding.addFolder.setOnClickListener {
            val editFolderDialog = EditFolderDialogFragment().apply {
                setMode(EditFolderDialogFragment.ADD_MODE)
                setFolderDao(folderDao)
            }

            editFolderDialog.show(requireActivity().supportFragmentManager, editFolderDialog.tag)
        }

        folderDao.getAll().observe(viewLifecycleOwner) { folders ->
            if (folders.isEmpty())
                binding.emptyMessage.crossFadeIn(200)
            else
                binding.emptyMessage.crossFadeOut(200)

            if (!initialized) {
                folderAdapter = FolderAdapter(folderDao) {
                    onFolderClickListener.invoke(it)
                }.apply {
                    submitList(folders)
                }

                binding.folderRecyclerView.layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
                binding.folderRecyclerView.adapter = folderAdapter
                initialized = true
            } else {
                folderAdapter.submitList(folders)
            }
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

}