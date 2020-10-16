package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import kotlinx.android.synthetic.main.item_diary.view.*

class DiaryAdapter
    : ListAdapter<DiaryModel, DiaryAdapter.ViewHolder>(DiaryDiffCallback()) {
    class ViewHolder private constructor(val view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_diary, parent, false)

                return ViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diary = getItem(position)
        holder.view.text_title.text = diary.title
    }
}

class DiaryDiffCallback: DiffUtil.ItemCallback<DiaryModel>() {
    override fun areItemsTheSame(oldItem: DiaryModel, newItem: DiaryModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DiaryModel, newItem: DiaryModel): Boolean {
        return oldItem == newItem
    }

}