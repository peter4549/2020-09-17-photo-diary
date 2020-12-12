package com.duke.elliot.kim.kotlin.photodiary.folder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.*

class SelectFolderDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentSelectFolderDialogBinding
    private lateinit var onFolderClickListener: (FolderModel) -> Unit

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
        val folderDao = database.folderDao()
        folderDao.getAll().observe(viewLifecycleOwner) { folders ->

        }

        return binding.root
    }
}