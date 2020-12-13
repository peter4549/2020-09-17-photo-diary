package com.duke.elliot.kim.kotlin.photodiary.folder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemFolderBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val DEFAULT_FOLDER_ID = -1L

class FolderAdapter(private val folderDao: FolderDao, private val onFolderClickListener: (FolderModel) -> Unit):
    ListAdapter<FolderModel, RecyclerView.ViewHolder>(FolderDiffCallback()) {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    inner class ViewHolder constructor(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(binding: ItemFolderBinding, folder: FolderModel) {
            binding.folderColor.setBackgroundColor(folder.color)
            binding.folderName.text = folder.name
            binding.editFolder.setOnClickListener {
                showPopupMenu(it, folder)
            }

            binding.cardView.setOnClickListener {
                onFolderClickListener.invoke(folder)
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(holder.binding as ItemFolderBinding, getItem(position))
    }

    private fun showPopupMenu(view: View, folder: FolderModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.folder_options)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    // TODO. change and update folder.
                    true
                }
                R.id.delete -> {
                    deleteFolder(folder)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun deleteFolder(folder: FolderModel) {
        coroutineScope.launch {
            folderDao.delete(folder)
        }
    }
}

class FolderDiffCallback: DiffUtil.ItemCallback<FolderModel>() {
    override fun areItemsTheSame(oldItem: FolderModel, newItem: FolderModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FolderModel, newItem: FolderModel): Boolean {
        return oldItem == newItem
    }
}