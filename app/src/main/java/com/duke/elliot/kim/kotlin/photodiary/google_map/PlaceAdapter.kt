package com.duke.elliot.kim.kotlin.photodiary.google_map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemPlaceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

const val PLACE_SELECTED = 1533L

class PlaceAdapter(private val onHashTagClickListener: (PlaceModel) -> Unit):
    ListAdapter<Pair<PlaceModel, Int>, RecyclerView.ViewHolder>(PlaceDiffCallback()) {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    inner class ViewHolder constructor(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(binding: ItemPlaceBinding, pair: Pair<PlaceModel, Int>) {
            binding.folderName.text = pair.first.name
            binding.itemCount.text = pair.second.toString()

            binding.cardView.setOnClickListener {
                onHashTagClickListener.invoke(pair.first)
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(holder.binding as ItemPlaceBinding, getItem(position))
    }
}

class PlaceDiffCallback: DiffUtil.ItemCallback<Pair<PlaceModel, Int>>() {
    override fun areItemsTheSame(oldItem: Pair<PlaceModel, Int>, newItem: Pair<PlaceModel, Int>): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Pair<PlaceModel, Int>, newItem: Pair<PlaceModel, Int>): Boolean {
        return oldItem == newItem
    }
}