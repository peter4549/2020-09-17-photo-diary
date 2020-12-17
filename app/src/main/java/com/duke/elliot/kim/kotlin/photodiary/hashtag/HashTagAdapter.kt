package com.duke.elliot.kim.kotlin.photodiary.hashtag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemHashtagBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

const val HASHTAG_SELECTED = -825L
// SHOW_FAVORITES is -2213L.
// DEFAULT_FOLDER_ID is -1L.

class HashTagAdapter(private val onHashTagClickListener: (String) -> Unit):
    ListAdapter<Pair<String, Int>, RecyclerView.ViewHolder>(HashTagDiffCallback()) {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    inner class ViewHolder constructor(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(binding: ItemHashtagBinding, hashTagPair: Pair<String, Int>) {
            binding.folderName.text = hashTagPair.first
            binding.itemCount.text = hashTagPair.second.toString()

            binding.cardView.setOnClickListener {
                onHashTagClickListener.invoke(hashTagPair.first)
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        val binding = ItemHashtagBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(holder.binding as ItemHashtagBinding, getItem(position))
    }
}

class HashTagDiffCallback: DiffUtil.ItemCallback<Pair<String, Int>>() {
    override fun areItemsTheSame(oldItem: Pair<String, Int>, newItem: Pair<String, Int>): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(oldItem: Pair<String, Int>, newItem: Pair<String, Int>): Boolean {
        return oldItem == newItem
    }
}