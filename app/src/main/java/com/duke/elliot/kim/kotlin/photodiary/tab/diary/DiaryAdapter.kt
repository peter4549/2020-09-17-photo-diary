package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.MainViewModel
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemDiaryBinding
import com.duke.elliot.kim.kotlin.photodiary.databinding.ViewModeSortBarBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.coroutines.*
import java.util.*
import java.util.Collections.unmodifiableList
import kotlin.Comparator


private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

private const val SORT_BY_LATEST = 0
private const val SORT_BY_OLDEST = 1

class DiaryAdapter : ListAdapter<AdapterItem, RecyclerView.ViewHolder>(DiaryDiffCallback()) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteOnClickListener: () -> Unit
    private lateinit var editOnClickListener: () -> Unit
    private lateinit var updateListener: () -> Unit
    private lateinit var viewOnClickListener: () -> Unit
    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private var currentItem: DiaryModel? = null
    private var sortingCriteria = SORT_BY_LATEST

    fun addHeaderAndSubmitList(list: List<DiaryModel>?) {
        adapterScope.launch {
            val items = when(list) {
                null -> listOf(AdapterItem.Header)
                else -> listOf(AdapterItem.Header) + list.map { AdapterItem.DiaryItem(it) }
            }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        val binding =
            ItemDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    private fun inflateHeaderFrom(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = ViewModeSortBarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeaderViewHolder(binding)
    }

    fun setViewOnClickListener(viewOnClickListener: () -> Unit) {
        this.viewOnClickListener = viewOnClickListener
    }

    fun setEditOnClickListener(editOnClickListener: () -> Unit) {
        this.editOnClickListener = editOnClickListener
    }

    fun setDeleteOnClickListener(deleteOnClickListener: () -> Unit) {
        this.deleteOnClickListener = deleteOnClickListener
    }

    fun setUpdateListener(updateListener: () -> Unit) {
        this.updateListener = updateListener
    }

    fun getCurrentDiary(): DiaryModel? = currentItem

    fun sort() {
        val modifiableList = ArrayList(currentList)
        Collections.sort(modifiableList,
            Comparator { o1: AdapterItem, o2: AdapterItem ->
                when (sortingCriteria) {
                    SORT_BY_LATEST -> {
                        sortingCriteria = SORT_BY_OLDEST
                        return@Comparator (o1.id - o2.id).toInt()
                    }
                    SORT_BY_OLDEST -> {
                        sortingCriteria = SORT_BY_LATEST
                        return@Comparator (o2.id - o1.id).toInt()
                    }
                    else -> 0
                }
            }
        )

        // recyclerView.scheduleLayoutAnimation()
        submitList(modifiableList)
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder constructor(val binding: ViewModeSortBarBinding): RecyclerView.ViewHolder(
        binding.root
    ) {
        private val viewModes = arrayOf(
            Pair(binding.root.context.getString(R.string.list), R.drawable.ic_sharp_view_list_24),
            Pair(binding.root.context.getString(R.string.brief_view), R.drawable.ic_sharp_list_24),
            Pair(binding.root.context.getString(R.string.frame), R.drawable.ic_four_squares_24)
        )

        private val viewModeAdapter = object : ArrayAdapter<Pair<String, Int>>(
            binding.root.context,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            viewModes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<View>(android.R.id.text1) as TextView

                textView.text = viewModes[position].first
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                textView.setCompoundDrawablesWithIntrinsicBounds(
                    viewModes[position].second,
                    0,
                    0,
                    0
                )

                textView.compoundDrawablePadding = (16 * binding.root.context.resources.displayMetrics.density + 0.5F).toInt()
                return view
            }
        }

        fun bind() {
            binding.viewModeContainer.setOnClickListener {
                // (recyclerView.layoutManager as StaggeredGridLayoutManager).spanCount = 1
                MaterialAlertDialogBuilder(binding.root.context)
                    .setTitle(binding.root.context.getString(R.string.view_mode))
                    .setAdapter(viewModeAdapter) { dialog, item ->
                        showToast(binding.root.context, "JIO")
                    }
                    .show()
            }

            binding.sortingContainer.setOnClickListener {
                sort()
            }
        }
    }

    inner class ViewHolder constructor(val binding: ItemDiaryBinding): RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(diary: DiaryModel) {
            val font = getFont(itemView.context, diary.textOptions.textFontId)

            binding.textDate.text = diary.time.toDateFormat(binding.root.context.getString(R.string.date_format_short))
            binding.textTime.text = diary.time.toDateFormat(binding.root.context.getString(R.string.time_format_short))
            binding.imageWeatherIcon.setImageResource(DiaryWritingViewModel.weatherIconIds[diary.weatherIconIndex])

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

            for (hashTag in diary.hashTags) {
                val chip = Chip(binding.chipGroup.context)
                chip.text = hashTag
                chip.setTextAppearanceResource(R.style.ChipFontStyle)
                chip.isCloseIconVisible = false
                binding.chipGroup.addView(chip)
            }

            if (diary.mediaArray.isEmpty())
                binding.mediaContainer.visibility = View.GONE
            else {
                binding.viewPager.removeAllViews()
                binding.viewPager.adapter = MediaPagerAdapter().apply {
                    setContext(itemView.context)
                    setMediaList(diary.mediaArray.toList())
                }
            }

            binding.buttonStar.isLiked = diary.liked

            binding.root.setOnClickListener {
                currentItem = diary
                MainViewModel.selectedDiaryPosition = absoluteAdapterPosition

                if (::viewOnClickListener.isInitialized)
                    viewOnClickListener.invoke()
            }

            binding.viewPager.setSingleTapUpListener {
                currentItem = diary
                MainViewModel.selectedDiaryPosition = absoluteAdapterPosition
                binding.root.callOnClick()
            }

            binding.imageDelete.setOnClickListener {
                currentItem = diary
                MainViewModel.selectedDiaryPosition = absoluteAdapterPosition

                if (::deleteOnClickListener.isInitialized)
                    deleteOnClickListener.invoke()
            }

            binding.imageEdit.setOnClickListener {
                currentItem = diary
                MainViewModel.selectedDiaryPosition = absoluteAdapterPosition

                if (::editOnClickListener.isInitialized)
                    editOnClickListener.invoke()
            }

            binding.buttonStar.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton?) {
                    binding.buttonStar.isEnabled = false
                    diary.liked = true
                    currentItem = diary
                    MainViewModel.selectedDiaryPosition = absoluteAdapterPosition

                    if (::updateListener.isInitialized)
                        updateListener.invoke()

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        binding.buttonStar.isEnabled = true
                    }
                }

                override fun unLiked(likeButton: LikeButton?) {
                    binding.buttonStar.isEnabled = false
                    diary.liked = false
                    currentItem = diary
                    MainViewModel.selectedDiaryPosition = absoluteAdapterPosition

                    if (::updateListener.isInitialized) {
                        updateListener.invoke()
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        binding.buttonStar.isEnabled = true
                    }
                }
            })
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AdapterItem.Header -> ITEM_VIEW_TYPE_HEADER
            is AdapterItem.DiaryItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_VIEW_TYPE_HEADER -> inflateHeaderFrom(parent)
            ITEM_VIEW_TYPE_ITEM -> from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                val diaryItem = getItem(position) as AdapterItem.DiaryItem
                holder.bind(diaryItem.diary)
            }
            is HeaderViewHolder -> {
                holder.bind()
            }
        }
    }
}

class DiaryDiffCallback: DiffUtil.ItemCallback<AdapterItem>() {
    override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem == newItem
    }
}

sealed class AdapterItem() {
    data class DiaryItem(val diary: DiaryModel): AdapterItem() {
        override val id = diary.id
    }

    object Header: AdapterItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}