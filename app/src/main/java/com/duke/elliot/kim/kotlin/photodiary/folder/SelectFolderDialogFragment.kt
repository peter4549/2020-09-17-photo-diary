package com.duke.elliot.kim.kotlin.photodiary.folder

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDao
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.databinding.*
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.crossFadeIn
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast

class SelectFolderDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentSelectFolderDialogBinding
    private lateinit var diaryDao: DiaryDao
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
        diaryDao = database.diaryDao()
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
                binding.emptyMessage.visibility = View.GONE

            if (!initialized) {
                folderAdapter = FolderAdapter(folderDao, diaryDao) {
                    onFolderClickListener.invoke(it)
                }.apply {
                    // submitList(folders.toList())
                    setOnEditFolderClickListener {
                        val editFolderDialog = EditFolderDialogFragment().apply {
                            setMode(EditFolderDialogFragment.EDIT_MODE)
                            setFolderDao(folderDao)
                            setFolder(it)
                            setCallbackAfterEditing { folder ->
                                val diaryWritingFragmentHandler = (requireActivity() as MainActivity).diaryWritingFragmentHandler
                                if (diaryWritingFragmentHandler != null) {
                                    val message = diaryWritingFragmentHandler.obtainMessage()
                                    message.what = DIARY_WRITING_HANDLER_FOLDER_CHANGED_MESSAGE
                                    message.obj = folder
                                    diaryWritingFragmentHandler.sendMessage(message)
                                }

                                val diariesFragmentHandler = (requireActivity() as MainActivity).diariesFragmentHandler
                                if (diariesFragmentHandler != null) {
                                    val message = diariesFragmentHandler.obtainMessage()
                                    message.what = DIARIES_FRAGMENT_HANDLER_FOLDER_CHANGED_MESSAGE
                                    diariesFragmentHandler.sendMessage(message)
                                }
                            }
                        }

                        editFolderDialog.show(requireActivity().supportFragmentManager, editFolderDialog.tag)
                    }
                }

                binding.folderRecyclerView.setHasFixedSize(false)
                binding.folderRecyclerView.layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
                binding.folderRecyclerView.scheduleLayoutAnimation()
                binding.folderRecyclerView.adapter = folderAdapter
                initialized = true
            }

            /*
            else {
                // When an item has been deleted.
                // if (folders.count() >= folderAdapter.itemCount)
                //     binding.folderRecyclerView.scheduleLayoutAnimation()
            }
             */

            folderAdapter.submitList(folders)
            folderAdapter.notifyDataSetChanged()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

}