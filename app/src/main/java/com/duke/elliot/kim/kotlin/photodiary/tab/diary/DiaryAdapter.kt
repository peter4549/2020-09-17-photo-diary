package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont
import kotlinx.android.synthetic.main.item_diary.view.*


class DiaryAdapter : ListAdapter<DiaryModel, DiaryAdapter.ViewHolder>(DiaryDiffCallback()) {

    private lateinit var viewClickListener: () -> Unit
    private var currentItemPosition = 0

    fun setViewClickListener(viewClickListener: () -> Unit) {
        this.viewClickListener = viewClickListener
    }

    fun getCurrentDiary(): DiaryModel = getItem(currentItemPosition)


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
        val font = getFont(holder.view.context, diary.textOptions.textFontId)

        holder.view.text_title.text = diary.title
        holder.view.text_content.text = diary.content

        holder.view.text_title.setTextColor(diary.textOptions.textColor)
        holder.view.text_content.setTextColor(diary.textOptions.textColor)

        holder.view.text_content.gravity = diary.textOptions.textAlignment

        holder.view.text_title.typeface = font
        holder.view.text_content.typeface = font

        if (diary.textOptions.textStyleBold && diary.textOptions.textStyleItalic)
            holder.view.text_content.setTypeface(font, Typeface.BOLD_ITALIC)
        else if (diary.textOptions.textStyleBold)
            holder.view.text_content.setTypeface(font, Typeface.BOLD)
        else if (diary.textOptions.textStyleItalic)
            holder.view.text_content.setTypeface(font, Typeface.ITALIC)

        holder.view.view_pager.adapter = MediaPagerAdapter().apply {
            setContext(holder.view.context)
            setMediaList(diary.mediaArray.toList())
        }

        holder.view.setOnClickListener {
            currentItemPosition = position

            if (::viewClickListener.isInitialized)
                viewClickListener.invoke()
        }

        holder.view.view_pager.setSingleTapUpListener {
            holder.view.callOnClick()
        }
    }

    private fun setBottomIconOnClickListeners() {

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