package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.MainViewModel
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemDiaryBinding
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemDiaryBriefViewBinding
import com.duke.elliot.kim.kotlin.photodiary.databinding.ItemDiaryFrameViewBinding
import com.duke.elliot.kim.kotlin.photodiary.databinding.ViewModeSortBarBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.export.ExportUtilities
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.item_select_dialog_28.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.Comparator

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

private const val SORT_BY_LATEST = 0
private const val SORT_BY_OLDEST = 1

const val LIST_VIEW_MODE = 0
const val BRIEF_VIEW_MODE = 1
const val FRAME_VIEW_MODE = 2

private const val EXPORT_AS_TEXT_FILE = 0
private const val EXPORT_AS_PDF_FILE = 1
private const val SHARE_DIARY = 2
private const val SEND_DIARY_TO_KAKAO_TALK = 3
private const val SEND_DIARY_TO_FACEBOOK = 4

private const val PREFERENCES_DIARY_ADAPTER = "preferences_diary_adapter"
private const val KEY_VIEW_MODE = "key_view_mode"
private const val KEY_SORTING_CRITERIA = "key_sorting_criteria"

class DiaryAdapter(private val context: Context, noInitialization: Boolean = false) : ListAdapter<AdapterItem, RecyclerView.ViewHolder>(
    DiaryDiffCallback()
) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteOnClickListener: () -> Unit
    private lateinit var editOnClickListener: () -> Unit
    private lateinit var convertPdfClickListener: () -> Unit
    private lateinit var updateListener: () -> Unit
    private lateinit var shareOnClickListener: () -> Unit
    private lateinit var sendDiaryToKakaoTalkClickListener: () -> Unit
    private lateinit var sendDiaryToFacebookClickListener: () -> Unit
    private lateinit var viewOnClickListener: () -> Unit
    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private var currentItem: DiaryModel? = null
    // TODO, load from shared pref.
    private var sortingCriteria = SORT_BY_LATEST
    var viewMode = LIST_VIEW_MODE

    private val exportTypes = arrayOf(
        Pair(context.getString(R.string.export_text), R.drawable.ic_text_file_24),
        Pair(context.getString(R.string.export_pdf_file), R.drawable.ic_pdf_file_24),
        Pair(context.getString(R.string.share_diary), R.drawable.ic_round_share_24),
        Pair(context.getString(R.string.send_diary_to_kakao_talk), R.drawable.ic_kakao_talk_150px),
        Pair(context.getString(R.string.send_diary_to_facebook), R.drawable.ic_facebook_24)
    )

    private val exportTypeAdapter = object : ArrayAdapter<Pair<String, Int>>(
        context,
        R.layout.item_select_dialog_28,
        R.id.text,
        exportTypes
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val text = view.text
            val image = view.image

            text.text = exportTypes[position].first
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
            image.setImageResource(exportTypes[position].second)

            return view
        }
    }

    private fun showExportTypeDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.export))
            .setAdapter(exportTypeAdapter) { _, exportType ->
                recyclerView.scheduleLayoutAnimation()
                when(exportType) {
                    EXPORT_AS_TEXT_FILE -> {
                        showInputDialog(
                            context,
                            context.getString(R.string.text_file_name_input_message)
                        ) { fileName ->
                            adapterScope.launch {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                    ExportUtilities.exportAsTextFileQ(
                                        context,
                                        getCurrentDiary(),
                                        fileName
                                    )
                                else
                                    ExportUtilities.exportAsTextFile(
                                        context,
                                        getCurrentDiary(),
                                        fileName
                                    )
                            }
                        }
                    }
                    EXPORT_AS_PDF_FILE -> {
                        if (::convertPdfClickListener.isInitialized)
                            convertPdfClickListener.invoke()
                    }
                    SHARE_DIARY -> {
                        if (::shareOnClickListener.isInitialized)
                            shareOnClickListener.invoke()
                    }
                    SEND_DIARY_TO_KAKAO_TALK -> {
                        if (ExportUtilities.isKakaoTalkInstalled(context)) {
                            if (::sendDiaryToKakaoTalkClickListener.isInitialized)
                                sendDiaryToKakaoTalkClickListener.invoke()
                        } else
                            showToast(context, context.getString(R.string.kakao_talk_not_found))
                    }
                    SEND_DIARY_TO_FACEBOOK -> {
                        if (ExportUtilities.isFacebookInstalled(context)) {
                            if (::sendDiaryToFacebookClickListener.isInitialized)
                                sendDiaryToFacebookClickListener.invoke()
                        } else
                            showToast(context, context.getString(R.string.facebook_not_found))
                    }
                }
            }
            .show()
    }

    init {
        if (!noInitialization)
            loadSortingCriteriaViewMode()
    }

    fun addHeaderAndSubmitList(list: List<DiaryModel>?) {
        adapterScope.launch {
            sort(list)

            val items = when(list) {
                null -> listOf(AdapterItem.Header)
                else -> listOf(AdapterItem.Header) + list.map { AdapterItem.DiaryItem(it) }
            }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun submitListWithoutHeader(list: List<DiaryModel>) {
        adapterScope.launch {
            withContext(Dispatchers.Main) {
                submitList(list.map { AdapterItem.DiaryItem(it) })
            }
        }
    }

    private fun from(parent: ViewGroup, viewMode: Int = 0): ViewHolder {
        val binding = when(viewMode) {
            LIST_VIEW_MODE -> ItemDiaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            BRIEF_VIEW_MODE -> ItemDiaryBriefViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            // FRAME_VIEW_MODE
            else -> ItemDiaryFrameViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        }

        return ViewHolder(binding)
    }

    private fun inflateHeaderFrom(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = ViewModeSortBarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        when(sortingCriteria) {
            SORT_BY_LATEST -> binding.textSort.text = context.getString(R.string.oldest)
            SORT_BY_OLDEST -> binding.textSort.text = context.getString(R.string.latest)
        }

        when(viewMode) {
            LIST_VIEW_MODE -> {
                binding.textViewMode.text = context.getString(R.string.list)
                binding.imageViewMode.setImageResource(R.drawable.ic_sharp_view_list_24)
            }
            BRIEF_VIEW_MODE -> {
                binding.textViewMode.text = context.getString(R.string.brief_view)
                binding.imageViewMode.setImageResource(R.drawable.ic_sharp_list_24)
            }
            FRAME_VIEW_MODE -> {
                binding.textViewMode.text = context.getString(R.string.frame)
                binding.imageViewMode.setImageResource(R.drawable.ic_four_squares_24)
            }
        }

        return HeaderViewHolder(binding)
    }

    fun setViewOnClickListener(viewOnClickListener: () -> Unit) {
        this.viewOnClickListener = viewOnClickListener
    }

    fun setEditOnClickListener(editOnClickListener: () -> Unit) {
        this.editOnClickListener = editOnClickListener
    }

    fun setConvertPdfClickListener(convertPdfClickListener: () -> Unit) {
        this.convertPdfClickListener = convertPdfClickListener
    }

    fun setDeleteOnClickListener(deleteOnClickListener: () -> Unit) {
        this.deleteOnClickListener = deleteOnClickListener
    }

    fun setUpdateListener(updateListener: () -> Unit) {
        this.updateListener = updateListener
    }

    fun setShareClickListener(shareOnClickListener: () -> Unit) {
        this.shareOnClickListener = shareOnClickListener
    }

    fun setSendDiaryToKakaoTalkClickListener(sendDiaryToKakaoTalkClickListener: () -> Unit) {
        this.sendDiaryToKakaoTalkClickListener = sendDiaryToKakaoTalkClickListener
    }

    fun setSendDiaryToFacebookClickListener(sendDiaryToFacebookClickListener: () -> Unit) {
        this.sendDiaryToFacebookClickListener = sendDiaryToFacebookClickListener
    }

    fun getCurrentDiary(): DiaryModel? = currentItem

    private fun sort(list: List<DiaryModel>?) {
        list?.let {
            Collections.sort(list,
                Comparator { o1: DiaryModel, o2: DiaryModel ->
                    when (sortingCriteria) {
                        SORT_BY_LATEST -> {
                            return@Comparator (o1.time - o2.time).toInt()
                        }
                        SORT_BY_OLDEST -> {
                            return@Comparator (o2.time - o1.time).toInt()
                        }
                        else -> 0
                    }
                }
            )
        }
    }

    fun sort() {
        val modifiableList = ArrayList(currentList).filterIsInstance<AdapterItem.DiaryItem>()
        Collections.sort(modifiableList,
            Comparator { o1: AdapterItem, o2: AdapterItem ->
                when (sortingCriteria) {
                    SORT_BY_LATEST -> {
                        return@Comparator (o2.time - o1.time).toInt()
                    }
                    SORT_BY_OLDEST -> {
                        return@Comparator (o1.time - o2.time).toInt()
                    }
                    else -> 0
                }
            }
        )

        recyclerView.scheduleLayoutAnimation()
        val diaries = modifiableList.toList()
            .requireNoNulls().map { it.diary }
        addHeaderAndSubmitList(diaries)
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
            // Select view mode.
            binding.viewModeContainer.setOnClickListener {
                MaterialAlertDialogBuilder(binding.root.context)
                    .setTitle(binding.root.context.getString(R.string.view_mode))
                    .setAdapter(viewModeAdapter) { _, viewMode ->
                        recyclerView.scheduleLayoutAnimation()
                        when(viewMode) {
                            LIST_VIEW_MODE -> {
                                (recyclerView.layoutManager as StaggeredGridLayoutManager).spanCount =
                                    1
                            }
                            BRIEF_VIEW_MODE -> {
                                (recyclerView.layoutManager as StaggeredGridLayoutManager).spanCount =
                                    1
                            }
                            FRAME_VIEW_MODE -> {
                                (recyclerView.layoutManager as StaggeredGridLayoutManager).spanCount =
                                    2
                            }
                        }

                        this@DiaryAdapter.viewMode = viewMode
                        recyclerView.adapter = this@DiaryAdapter
                    }
                    .show()
            }

            binding.sortingContainer.setOnClickListener {
                sort()
                sortingCriteria = 1 - sortingCriteria

                if (sortingCriteria == SORT_BY_OLDEST)
                    binding.textSort.text = binding.root.context.getString(R.string.latest)
                else
                    binding.textSort.text = binding.root.context.getString(R.string.oldest)
            }
        }
    }

    inner class ViewHolder constructor(val binding: ViewDataBinding): RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(binding: ItemDiaryBriefViewBinding, diary: DiaryModel) {
            val font = getFont(itemView.context, diary.textOptions.textFontId)

            binding.textDate.text = diary.time.toDateFormat(binding.root.context.getString(R.string.date_format_short))
            binding.textTime.text = diary.time.toDateFormat(binding.root.context.getString(R.string.time_format_short))
            setImage(
                binding.imageWeatherIcon,
                DiaryWritingViewModel.weatherIconIds[diary.weatherIconIndex]
            )
            binding.textTitle.text = diary.title
            binding.textTitle.setTextColor(diary.textOptions.textColor)
            binding.textTitle.typeface = font

            if (diary.hashTags.isEmpty())
                binding.textHashTags.visibility = View.GONE
            else {
                binding.textHashTags.visibility = View.VISIBLE
                binding.textHashTags.text = diary.hashTags.joinToString(separator = " ")
            }

            if (diary.mediaArray.isEmpty())
                binding.imageMedia.visibility = View.GONE
            else {
                binding.imageMedia.visibility = View.VISIBLE
                setImage(binding.imageMedia, diary.mediaArray[0].uriString)
            }

            binding.root.setOnClickListener {
                currentItem = diary
                MainViewModel.selectedDiaryPosition = absoluteAdapterPosition

                if (::viewOnClickListener.isInitialized)
                    viewOnClickListener.invoke()
            }
        }

        fun bind(binding: ItemDiaryBinding, diary: DiaryModel) {
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
                binding.mediaContainer.visibility = View.VISIBLE
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

                    adapterScope.launch(Dispatchers.Main) {
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

                    adapterScope.launch(Dispatchers.Main) {
                        delay(200)
                        binding.buttonStar.isEnabled = true
                    }
                }
            })

            binding.imageExport.setOnClickListener {
                currentItem = diary
                showExportTypeDialog(binding.root.context)
            }

            binding.imageMore.setOnClickListener {
                currentItem = diary
                showPopupMenu(context, it!!, diary, absoluteAdapterPosition)
            }
        }

        fun bind(binding: ItemDiaryFrameViewBinding, diary: DiaryModel) {
            val font = getFont(itemView.context, diary.textOptions.textFontId)

            binding.textDate.text = diary.time.toDateFormat(binding.root.context.getString(R.string.date_format_short))
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

            if (diary.mediaArray.isEmpty())
                binding.mediaContainer.visibility = View.GONE
            else {
                binding.mediaContainer.visibility = View.VISIBLE
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

            binding.imageExport.setOnClickListener {
                showExportTypeDialog(binding.root.context)
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

            binding.imageMore.setOnClickListener {
                showPopupMenu(context, it!!, diary, absoluteAdapterPosition)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView

        if (viewMode == FRAME_VIEW_MODE)
            (this.recyclerView.layoutManager as StaggeredGridLayoutManager).spanCount = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AdapterItem.Header -> ITEM_VIEW_TYPE_HEADER
            is AdapterItem.DiaryItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // TODO: Divide target view.
        println("CREATED CALL!!! vm: $viewMode")
        return when(viewType) {
            ITEM_VIEW_TYPE_HEADER -> inflateHeaderFrom(parent)
            ITEM_VIEW_TYPE_ITEM -> {
                when (viewMode) {
                    LIST_VIEW_MODE -> from(parent)
                    BRIEF_VIEW_MODE -> from(parent, BRIEF_VIEW_MODE)
                    else -> from(parent, FRAME_VIEW_MODE)
                }
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                val diaryItem = getItem(position) as AdapterItem.DiaryItem
                when (viewMode) {
                    LIST_VIEW_MODE -> holder.bind(
                        holder.binding as ItemDiaryBinding,
                        diaryItem.diary
                    )
                    BRIEF_VIEW_MODE -> holder.bind(
                        holder.binding as ItemDiaryBriefViewBinding,
                        diaryItem.diary
                    )
                    FRAME_VIEW_MODE -> holder.bind(
                        holder.binding as ItemDiaryFrameViewBinding,
                        diaryItem.diary
                    )
                }
            }
            is HeaderViewHolder -> {
                holder.bind()
            }
        }
    }

    private fun showPopupMenu(context: Context, view: View, diary: DiaryModel, position: Int) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.diary_options)
        popupMenu.setOnMenuItemClickListener { item ->
            currentItem = diary
            MainViewModel.selectedDiaryPosition = position

            when (item.itemId) {
                R.id.edit -> {
                    if (::editOnClickListener.isInitialized)
                        editOnClickListener.invoke()
                    true
                }
                R.id.set_category ->  // TODO: Implement
                    true
                R.id.export -> {
                    showExportTypeDialog(context)
                    true
                }
                R.id.delete -> {
                    if (::deleteOnClickListener.isInitialized)
                        deleteOnClickListener.invoke()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    fun saveSortingCriteriaViewMode() {
        val sharedPreferences = context.getSharedPreferences(
            PREFERENCES_DIARY_ADAPTER,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()

        editor.putInt(KEY_SORTING_CRITERIA, sortingCriteria)
        editor.putInt(KEY_VIEW_MODE, viewMode)
        editor.apply()
    }

    private fun loadSortingCriteriaViewMode() {
        val sharedPreferences = context.getSharedPreferences(
            PREFERENCES_DIARY_ADAPTER,
            Context.MODE_PRIVATE
        )
        sortingCriteria = sharedPreferences.getInt(KEY_SORTING_CRITERIA, SORT_BY_LATEST)
        viewMode = sharedPreferences.getInt(KEY_VIEW_MODE, LIST_VIEW_MODE)
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

sealed class AdapterItem {
    data class DiaryItem(val diary: DiaryModel): AdapterItem() {
        override val id = diary.id
        override val time = diary.time
    }

    object Header: AdapterItem() {
        override val id = Long.MIN_VALUE
        override val time = Long.MIN_VALUE
    }

    abstract val id: Long
    abstract val time: Long
}