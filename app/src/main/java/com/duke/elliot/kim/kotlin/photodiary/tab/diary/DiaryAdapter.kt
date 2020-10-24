package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemDiaryBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import com.like.LikeButton
import com.like.OnLikeListener

class DiaryAdapter : ListAdapter<DiaryModel, DiaryAdapter.ViewHolder>(DiaryDiffCallback()) {

    private lateinit var deleteOnClickListener: () -> Unit
    private lateinit var updateListener: () -> Unit
    private lateinit var viewOnClickListener: () -> Unit
    private var currentItem: DiaryModel? = null

    private fun from(parent: ViewGroup): ViewHolder {
        val binding =
            ItemDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    fun setViewOnClickListener(viewOnClickListener: () -> Unit) {
        this.viewOnClickListener = viewOnClickListener
    }

    fun setDeleteOnClickListener(deleteOnClickListener: () -> Unit) {
        this.deleteOnClickListener = deleteOnClickListener
    }

    fun setUpdateListener(updateListener: () -> Unit) {
        this.updateListener = updateListener
    }

    fun getCurrentDiary(): DiaryModel? = currentItem

    inner class ViewHolder constructor(val binding: ItemDiaryBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(diary: DiaryModel) {
            val font = getFont(itemView.context, diary.textOptions.textFontId)

            binding.textDate.text = diary.time.toDateFormat(binding.root.context.getString(R.string.date_format_short))
            binding.textTime.text = diary.time.toDateFormat(binding.root.context.getString(R.string.time_format_short))
            binding.imageWeatherIcon.setImageResource(diary.weatherIconId)

            binding.textTitle.text = diary.title
            binding.textContent.text = diary.content

            binding.textTitle.setTextColor(diary.textOptions.textColor)
            binding.textContent.setTextColor(diary.textOptions.textColor)

            binding.textContent.gravity = diary.textOptions.textAlignment

            binding.textTitle.typeface = font
            binding.textContent.typeface = font

            if (diary.textOptions.textStyleBold && diary.textOptions.textStyleItalic)
                binding.textContent.setTypeface(font, Typeface.BOLD_ITALIC)
            else if (diary.textOptions.textStyleBold)
                binding.textContent.setTypeface(font, Typeface.BOLD)
            else if (diary.textOptions.textStyleItalic)
                binding.textContent.setTypeface(font, Typeface.ITALIC)

            if (diary.mediaArray.isEmpty())
                binding.mediaContainer.visibility = View.GONE
            else {
                binding.viewPager.adapter = MediaPagerAdapter().apply {
                    setContext(itemView.context)
                    setMediaList(diary.mediaArray.toList())
                }
            }

            binding.buttonStar.isLiked = diary.liked

            binding.root.setOnClickListener {
                currentItem = diary

                if (::viewOnClickListener.isInitialized)
                    viewOnClickListener.invoke()
            }

            binding.viewPager.setSingleTapUpListener {
                currentItem = diary
                binding.root.callOnClick()
            }

            binding.imageDelete.setOnClickListener {
                currentItem = diary

                if (::deleteOnClickListener.isInitialized)
                    deleteOnClickListener.invoke()
            }

            binding.buttonStar.setOnLikeListener(object: OnLikeListener {
                override fun liked(likeButton: LikeButton?) {
                    diary.liked = true
                    currentItem = diary

                    if (::updateListener.isInitialized)
                        updateListener.invoke()
                }

                override fun unLiked(likeButton: LikeButton?) {
                    diary.liked = false
                    currentItem = diary

                    if (::updateListener.isInitialized)
                        updateListener.invoke()
                }

            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diary = getItem(position)
        holder.bind(diary)
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