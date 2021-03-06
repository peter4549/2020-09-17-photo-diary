package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDiaryWritingBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.ExoPlayerActivity
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.photo_editor.PhotoEditorFragment
import com.duke.elliot.kim.kotlin.photodiary.folder.DEFAULT_FOLDER_ID
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel
import com.duke.elliot.kim.kotlin.photodiary.folder.SelectFolderDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.google_map.GoogleMapActivity
import com.duke.elliot.kim.kotlin.photodiary.google_map.KEY_GOOGLE_MAP_PLACE
import com.duke.elliot.kim.kotlin.photodiary.google_map.PlaceModel
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import com.google.android.material.chip.Chip
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

const val CREATE_MODE = 0
const val EDIT_MODE = 1
const val DATE_OTHER_THAN_TODAY = 2

const val PLACE_REQUEST_CODE = 1052

class DiaryWritingFragment: Fragment() {

    private lateinit var binding: FragmentDiaryWritingBinding
    private lateinit var fileUtilities: FileUtilities
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var progressDialogFragment: ProgressDialogFragment
    private lateinit var viewModel: DiaryWritingViewModel
    private lateinit var viewModelFactory: DiaryWritingViewModelFactory
    private var keyboardShown = false
    private var layoutOptionsHeight = 0F
    private var layoutOptionsIsShown = true
    private var layoutOptionItemsHeight = 0F
    private var layoutOptionItemsIsShown = false
    private var layoutOptionsWasShown = false
    private var mediumAnimationDuration = 0
    private var recyclerViewMediaIsShown = false
    private var selectedItemView: View? = null
    private var selectedTime = -1L
    private var shortAnimationDuration = 0
    private var showOptionItemsScheduled = false

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val optionsOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.image_drawing -> MediaHelper.startDrawingActivity(this)
            else -> {
                if (keyboardShown) {
                    layoutOptionsWasShown = false
                    showOptionItemsScheduled = true
                    selectedItemView = view
                    hideKeyboard(view, inputMethodManager)
                } else
                    showOptionItems(view)
            }
        }
    }

    private val editTextOnFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
        if (hasFocus && !keyboardShown && layoutOptionItemsIsShown) {
            binding.layoutOptionItems
                .hideDownWithFading(
                    shortAnimationDuration,
                    layoutOptionItemsHeight
                )
            binding.optionItemsBackground
                .hideDown(shortAnimationDuration, layoutOptionItemsHeight)
            binding.layoutOptionsContainer
                .translateDown(shortAnimationDuration, layoutOptionItemsHeight) {
                    showKeyboard(view, inputMethodManager)
                }
            layoutOptionItemsIsShown = false
        } else if (hasFocus && !keyboardShown) {
            showKeyboard(view, inputMethodManager)
        }
    }

    private val optionItemsOnClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.image_camera_item -> MediaHelper.photoHelper.dispatchImageCaptureIntent(this)
            R.id.image_photo_item -> MediaHelper.photoHelper.dispatchImagePickerIntent(this, false)
            R.id.image_photo_library_item -> MediaHelper.photoHelper.dispatchImagePickerIntent(
                this,
                true
            )
            R.id.image_video_item -> MediaHelper.videoHelper.dispatchVideoPickerIntent(this, false)
            R.id.image_video_library_item -> MediaHelper.videoHelper.dispatchVideoPickerIntent(
                this,
                true
            )
            R.id.image_audio_item -> MediaHelper.audioHelper.dispatchAudioPickerIntent(this, false)
            R.id.image_audio_library_item -> MediaHelper.audioHelper.dispatchAudioContentPickerIntent(
                this
            )
        }
    }

    private val textOptionItemsOnClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.image_bold -> {
                binding.editTextTitle.typeface = null
                binding.editTextContent.typeface = null
                if (viewModel.textStyleBold) {
                    if (viewModel.textStyleItalic) {
                        binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.ITALIC)
                        binding.editTextContent.setTypeface(viewModel.textFont, Typeface.ITALIC)
                    } else {
                        binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.NORMAL)
                        binding.editTextContent.setTypeface(viewModel.textFont, Typeface.NORMAL)
                    }
                } else {
                    if (viewModel.textStyleItalic) {
                        binding.editTextTitle.setTypeface(
                            viewModel.textFont,
                            Typeface.BOLD_ITALIC
                        )
                        binding.editTextContent.setTypeface(
                            viewModel.textFont,
                            Typeface.BOLD_ITALIC
                        )
                    } else {
                        binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.BOLD)
                        binding.editTextContent.setTypeface(viewModel.textFont, Typeface.BOLD)
                    }
                }

                viewModel.textStyleBold = !viewModel.textStyleBold
                binding.imageBold.setColorFilter(
                    if (viewModel.textStyleBold)
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorTextOptionItemsSelected
                        )
                    else
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorTextOptionItemsUnselected
                        ),
                    PorterDuff.Mode.SRC_IN
                )
            }
            R.id.image_italic -> {
                if (viewModel.textStyleItalic) {
                    if (viewModel.textStyleBold) {
                        binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.BOLD)
                        binding.editTextContent.setTypeface(viewModel.textFont, Typeface.BOLD)
                    } else {
                        binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.NORMAL)
                        binding.editTextContent.setTypeface(viewModel.textFont, Typeface.NORMAL)
                    }
                } else {
                    if (viewModel.textStyleBold) {
                        binding.editTextTitle.setTypeface(
                            binding.editTextTitle.typeface,
                            Typeface.BOLD_ITALIC
                        )
                        binding.editTextContent.setTypeface(
                            binding.editTextContent.typeface,
                            Typeface.BOLD_ITALIC
                        )
                    } else {
                        binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.ITALIC)
                        binding.editTextContent.setTypeface(viewModel.textFont, Typeface.ITALIC)
                    }
                }

                viewModel.textStyleItalic = !viewModel.textStyleItalic
                binding.imageItalic.setColorFilter(
                    if (viewModel.textStyleItalic)
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorTextOptionItemsSelected
                        )
                    else
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorTextOptionItemsUnselected
                        ),
                    PorterDuff.Mode.SRC_IN
                )
            }
            R.id.image_button_text_align_center -> {
                binding.imageButtonTextAlignCenter.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.colorTextOptionItemsSelected),
                    PorterDuff.Mode.SRC_IN
                )
                binding.imageButtonTextAlignLeft.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsUnselected
                    ),
                    PorterDuff.Mode.SRC_IN
                )
                binding.imageButtonTextAlignRight.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsUnselected
                    ),
                    PorterDuff.Mode.SRC_IN
                )

                binding.editTextContent.gravity = Gravity.CENTER_HORIZONTAL
                viewModel.textAlignment = Gravity.CENTER_HORIZONTAL
            }
            R.id.image_button_text_align_left -> {
                binding.imageButtonTextAlignCenter.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsUnselected
                    ),
                    PorterDuff.Mode.SRC_IN
                )
                binding.imageButtonTextAlignLeft.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.colorTextOptionItemsSelected),
                    PorterDuff.Mode.SRC_IN
                )
                binding.imageButtonTextAlignRight.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsUnselected
                    ),
                    PorterDuff.Mode.SRC_IN
                )
                binding.editTextContent.gravity = Gravity.START
                viewModel.textAlignment = Gravity.START
            }
            R.id.image_button_text_align_right -> {
                binding.imageButtonTextAlignCenter.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsUnselected
                    ),
                    PorterDuff.Mode.SRC_IN
                )
                binding.imageButtonTextAlignLeft.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsUnselected
                    ),
                    PorterDuff.Mode.SRC_IN
                )
                binding.imageButtonTextAlignRight.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.colorTextOptionItemsSelected),
                    PorterDuff.Mode.SRC_IN
                )
                binding.editTextContent.gravity = Gravity.END
                viewModel.textAlignment = Gravity.END
            }
            R.id.image_text_color -> {
                showColorPickerDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_diary_writing,
            container,
            false
        )

        initializeToolbar(binding.toolbar)
        applyPrimaryThemeColor(binding.toolbar)
        applySecondaryThemeColor(binding.container, binding.frameLayoutDropdown)

        progressDialogFragment = ProgressDialogFragment.instance

        val diaryWritingFragmentArgs by navArgs<DiaryWritingFragmentArgs>()

        selectedTime = diaryWritingFragmentArgs.selectedTime

        viewModelFactory = DiaryWritingViewModelFactory(
            requireActivity().application,
            diaryWritingFragmentArgs.diary,
            diaryWritingFragmentArgs.mode
        )
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryWritingViewModel::class.java]

        fileUtilities = FileUtilities.getInstance(requireContext())

        inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hideKeyboard(binding.root, inputMethodManager)

        binding.textDate.text = viewModel.time.toDateFormat(getString(R.string.date_format))
        binding.textTime.text = viewModel.time.toDateFormat(getString(R.string.time_format))

        binding.diaryWritingViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        /** Folders Observe */
        (requireActivity() as MainActivity).getDiaryFolders()?.observe(viewLifecycleOwner, { folders ->
            viewModel.folders = folders
        })

        viewModel.action = DiaryWritingViewModel.Action.UNINITIALIZED
        if (viewModel.mediaArrayListSize > 0)
            binding.recyclerViewMedia.visibility = View.VISIBLE
        viewModel.mediaArrayList.observe(viewLifecycleOwner, { mediaArrayList ->
            when (viewModel.action) {
                DiaryWritingViewModel.Action.UNINITIALIZED -> {
                    mediaAdapter = MediaAdapter(R.layout.item_media, mediaArrayList).apply {
                        setItemClickListener {
                            viewModel.selectedItemPosition = getSelectedPosition()
                            when (it.type) {
                                MediaHelper.MediaType.PHOTO -> navigateToPhotoEditorFragment(it.uriString.toUri())
                                MediaHelper.MediaType.VIDEO -> startExoPlayerActivity(it.uriString.toUri())
                                MediaHelper.MediaType.AUDIO -> startExoPlayerActivity(it.uriString.toUri())
                                else -> throw ClassCastException("Invalid media type.")
                            }
                        }

                        setFileUtilities(fileUtilities)
                    }
                    binding.recyclerViewMedia.apply {
                        adapter = mediaAdapter
                        layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                            orientation = LinearLayoutManager.HORIZONTAL
                        }
                    }
                    viewModel.action = DiaryWritingViewModel.Action.INITIALIZED
                }
                DiaryWritingViewModel.Action.ADDED -> {
                    mediaAdapter.notifyItemInserted(viewModel.mediaArrayListSize)
                }
            }
        })

        binding.recyclerViewMedia.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorMediaRecyclerViewBackground
            )
        )

        mediumAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        layoutOptionsHeight = convertDpToPx(
            requireContext(),
            resources.getDimension(R.dimen.dimen_layout_options_height) / resources.displayMetrics.density
        )
        layoutOptionItemsHeight = convertDpToPx(
            requireContext(),
            resources.getDimension(R.dimen.dimen_layout_option_items_height) / resources.displayMetrics.density
        )

        binding.editTextTitle.showSoftInputOnFocus = false
        binding.editTextContent.showSoftInputOnFocus = false

        binding.editTextTitle.onFocusChangeListener = editTextOnFocusChangeListener
        binding.editTextContent.onFocusChangeListener = editTextOnFocusChangeListener

        binding.frameLayoutDropdown.setOnClickListener {
            when {
                layoutOptionItemsIsShown -> {
                    binding.layoutOptionItems
                        .hideDown(shortAnimationDuration, layoutOptionItemsHeight)
                    binding.optionItemsBackground
                        .hideDown(mediumAnimationDuration, layoutOptionItemsHeight)
                    binding.layoutOptionsContainer
                        .translateDown(mediumAnimationDuration, layoutOptionItemsHeight)
                    layoutOptionItemsIsShown = false
                }
                layoutOptionsIsShown -> {
                    binding.imageDropdown.rotate(180F, shortAnimationDuration)
                    binding.layoutOptionsContainer
                        .hideDown(shortAnimationDuration, layoutOptionsHeight)
                    layoutOptionsIsShown = false
                }
                else -> {
                    binding.imageDropdown.rotate(0F, shortAnimationDuration)
                    binding.layoutOptionsContainer
                        .showUp(shortAnimationDuration, layoutOptionsHeight)
                    layoutOptionsIsShown = true
                }
            }
        }

        binding.imagePhoto.setOnClickListener(optionsOnClickListener)
        binding.imageVideo.setOnClickListener(optionsOnClickListener)
        binding.imageAudio.setOnClickListener(optionsOnClickListener)
        binding.imageDrawing.setOnClickListener(optionsOnClickListener)
        binding.imageText.setOnClickListener(optionsOnClickListener)

        if (!viewModel.initialized) {
            viewModel.originDiary?.let { fetchOriginDiary(it) }
            viewModel.initialized = true
        }

        initializeSpinners()
        initializeOptionItems()
        initializeTextItems()
        initChipGroup()
        initPlaceChipGroup()

        binding.imageFolder.setOnClickListener {
            if (layoutOptionItemsIsShown) {
                binding.layoutOptionItems
                    .hideDownWithFading(
                        shortAnimationDuration,
                        layoutOptionItemsHeight
                    )
                binding.optionItemsBackground
                    .hideDown(shortAnimationDuration, layoutOptionItemsHeight)
                binding.layoutOptionsContainer
                    .translateDown(shortAnimationDuration, layoutOptionItemsHeight)
                layoutOptionItemsIsShown = false
            }

            val selectFolderDialog = SelectFolderDialogFragment().apply {
                setOnFolderClickListener {
                    addFolderChip(it)
                    dismiss()
                }
            }

            selectFolderDialog.show(requireActivity().supportFragmentManager, selectFolderDialog.tag)
        }

        setCursorColor(viewModel.textColor)
        applyTextOptions()  // TODO 여기서 bold, alignment, 색상 다시 할당할 것.

        setEventListener(
            requireActivity(),
            binding.lifecycleOwner ?: viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (!isOpen) {
                        binding.editTextTitle.clearFocus()
                        binding.editTextContent.clearFocus()

                        if (showOptionItemsScheduled) {
                            showOptionItemsScheduled = false
                            selectedItemView?.let { showOptionItems(it) }
                        }

                        if (layoutOptionsWasShown) {
                            binding.imageDropdown.rotate(0F, shortAnimationDuration)
                            binding.layoutOptionsContainer
                                .showUp(shortAnimationDuration, layoutOptionsHeight)
                            layoutOptionsIsShown = true
                            layoutOptionsWasShown = false
                        }

                    } else {  // isOpen == true
                        if (layoutOptionsIsShown) {
                            layoutOptionsWasShown = true
                            binding.imageDropdown.rotate(180F, shortAnimationDuration)
                            binding.layoutOptionsContainer
                                .hideDown(shortAnimationDuration, layoutOptionsHeight)
                            layoutOptionsIsShown = false
                        }
                    }

                    keyboardShown = isOpen
                }
            })

        findNavController().currentBackStackEntry
            ?.savedStateHandle?.get<Uri>(PhotoEditorFragment.KEY_EDITED_IMAGE_URI)
            ?.let { uri ->
                findNavController().currentBackStackEntry?.savedStateHandle
                    ?.remove<Uri>(PhotoEditorFragment.KEY_EDITED_IMAGE_URI)
                viewModel.selectedItemPosition?.let { position ->
                    mediaAdapter.changeImageUri(position, uri)
                }
            }

        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        /** Handler */
        (requireActivity() as MainActivity).diaryWritingFragmentHandler = Handler(Looper.getMainLooper()) {
            when(it.what) {
                DIARY_WRITING_HANDLER_FOLDER_CHANGED_MESSAGE -> {
                    viewModel.folder?.let { folder ->
                        if (folder.id == (it.obj as FolderModel).id) {
                            viewModel.folder = it.obj as FolderModel
                            viewModel.folder?.let { newFolder -> addFolderChip(newFolder) }
                        }
                    }
                }
            }
            true
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.mode == DATE_OTHER_THAN_TODAY) {
            showConfirmDateDialog(selectedTime)
        }
    }

    private fun backPressed() {
        if (isChanged()) {
            val saveDiaryDialogFragment = OkCancelDialogFragment().apply {
                val message = if (viewModel.mode == CREATE_MODE || viewModel.mode == DATE_OTHER_THAN_TODAY)
                    binding.root.context.getString(R.string.save_diary_message)
                else
                    binding.root.context.getString(R.string.save_edited_diary_message)

                setDialogParameters(
                    binding.root.context.getString(R.string.save_diary_title),
                    message
                ) {
                    if (viewModel.mode == CREATE_MODE || viewModel.mode == DATE_OTHER_THAN_TODAY)
                        saveDiary(createDiary())
                    else
                        updateDiary()

                    this.dismiss()
                }

                setCancelClickEvent {
                    findNavController().popBackStack()
                }

                setButtonTexts(
                    okButtonText = binding.root.context.getString(R.string.yes),
                    cancelButtonText = binding.root.context.getString(R.string.no)
                )
            }

            saveDiaryDialogFragment.show(requireActivity().supportFragmentManager, saveDiaryDialogFragment.tag)
        } else
            findNavController().popBackStack()
    }

    override fun onStart() {
        super.onStart()
        binding.editTextTitle.setText(viewModel.title)
        binding.editTextContent.setText(viewModel.content)
    }

    override fun onStop() {
        super.onStop()
        viewModel.title = binding.editTextTitle.text.toString()
        viewModel.content = binding.editTextContent.text.toString()

        viewModel.updateHashTagsToPreferences()
        PhotoHelper.deleteTempJpegFiles(requireContext())
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun fetchOriginDiary(originDiary: DiaryModel) {
        viewModel.title = originDiary.title
        viewModel.content = originDiary.content

        /** The mediaArray and spinner options are assigned as the value of the live data of the view model,
         *  so there is no need to fetch it here. */
    }

    private fun initializeOptionItems() {
        binding.imageCameraItem.setOnClickListener(optionItemsOnClickListener)
        binding.imagePhotoItem.setOnClickListener(optionItemsOnClickListener)
        binding.imagePhotoLibraryItem.setOnClickListener(optionItemsOnClickListener)
        binding.imageVideoItem.setOnClickListener(optionItemsOnClickListener)
        binding.imageVideoLibraryItem.setOnClickListener(optionItemsOnClickListener)
        binding.imageAudioItem.setOnClickListener(optionItemsOnClickListener)
        binding.imageAudioLibraryItem.setOnClickListener(optionItemsOnClickListener)
    }

    private fun initChipGroup() {
        binding.chipGroup.removeAllViews()

        /** Folder */
        coroutineScope.launch(Dispatchers.Default) {
            DiaryDatabase.getInstance(requireContext()).folderDao().let { folderDao ->
                viewModel.originDiary?.let {
                    viewModel.folder = folderDao.getFolderById(it.folderId)
                    viewModel.folder?.let { folder ->
                        withContext(Dispatchers.Main) {
                            addFolderChip(folder)
                        }
                    }
                }
            }
        }

        /** Hash Tag */
        for (hashTag in viewModel.selectedHashTags) {
            val chip = Chip(requireContext())
            chip.text = hashTag
            chip.setTextAppearanceResource(R.style.ChipFontStyle)
            chip.setOnCloseIconClickListener {
                binding.chipGroup.removeView(it)
                viewModel.selectedHashTags.remove((it as Chip).text)
            }

            binding.chipGroup.addView(chip)
        }
    }

    private fun initPlaceChipGroup() {
        viewModel.place?.let { addPlaceChip(it) }
    }

    private fun initializeSpinners() {
        binding.spinnerFont.adapter = FontSpinnerAdapter(
            requireContext(), resources.getStringArray(R.array.fonts)
        )

        val fontSizes = arrayOf(12, 14, 16, 18, 20, 22, 24)

        binding.spinnerTextSize.adapter =
           ArrayAdapter(requireContext(), R.layout.item_spinner, fontSizes)

        binding.spinnerTextSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                viewModel.textSize = fontSizes[position].toFloat()
                binding.editTextTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, viewModel.textSize)
                binding.editTextContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, viewModel.textSize)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {  }
        }

        binding.spinnerFont.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val fontName = binding.spinnerFont.selectedItem.toString()
                viewModel.textFontId = MainActivity.fontNameIdMap[fontName] ?: MainActivity.DEFAULT_FONT_ID
                viewModel.textFont = getFont(requireContext(), viewModel.textFontId)
                binding.editTextTitle.typeface = viewModel.textFont
                binding.editTextContent.typeface = viewModel.textFont

                if (viewModel.textStyleBold && viewModel.textStyleItalic) {
                    binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.BOLD_ITALIC)
                    binding.editTextContent.setTypeface(viewModel.textFont, Typeface.BOLD_ITALIC)
                } else if (viewModel.textStyleBold) {
                    binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.BOLD)
                    binding.editTextContent.setTypeface(viewModel.textFont, Typeface.BOLD)
                } else if (viewModel.textStyleItalic) {
                    binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.ITALIC)
                    binding.editTextContent.setTypeface(viewModel.textFont, Typeface.ITALIC)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {  }
        }

        binding.spinnerWeather.apply {
            adapter = WeatherSpinnerAdapter(requireContext(), DiaryWritingViewModel.weatherIconIds)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.weatherIconIndex = position
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {  }
            }
        }

        binding.spinnerHashTag.apply {
            adapter = HashTagSpinnerAdapter(requireContext(), viewModel.hashTagList).apply {
                setDeleteButtonClickListener { position ->
                    try {
                        val method =
                            Spinner::class.java.getDeclaredMethod("onDetachedFromWindow")
                        method.isAccessible = true
                        method.invoke(binding.spinnerHashTag)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val hashTag = viewModel.hashTagList[position]
                    viewModel.deleteHashTagInPreferences(hashTag)
                }
            }
            isSelected = false
            setSelection(0, true)
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        showHashTagInputDialog()
                    } else
                        addHashTagChip(viewModel.hashTagList[position])
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {  }
            }
        }

        binding.spinnerWeather.setSelection(viewModel.weatherIconIndex)
        binding.spinnerTextSize.setSelection(fontSizes.indexOf(viewModel.textSize.toInt()))  // 18sp
        binding.spinnerFont.setSelection(MainActivity.fontIds.indexOf(viewModel.textFontId))
    }

    private fun initializeTextItems() {
        binding.imageBold.setOnClickListener(textOptionItemsOnClickListener)
        binding.imageItalic.setOnClickListener(textOptionItemsOnClickListener)
        binding.imageButtonTextAlignCenter.setOnClickListener(textOptionItemsOnClickListener)
        binding.imageButtonTextAlignLeft.setOnClickListener(textOptionItemsOnClickListener)
        binding.imageButtonTextAlignRight.setOnClickListener(textOptionItemsOnClickListener)
        binding.imageTextColor.setOnClickListener(textOptionItemsOnClickListener)

        if (viewModel.textStyleBold)
            binding.imageBold.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorTextOptionItemsSelected
                )
            )

        if (viewModel.textStyleItalic)
            binding.imageItalic.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorTextOptionItemsSelected
                )
            )

        binding.imageButtonTextAlignCenter.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextOptionItemsUnselected
            )
        )
        binding.imageButtonTextAlignLeft.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextOptionItemsUnselected
            )
        )
        binding.imageButtonTextAlignRight.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextOptionItemsUnselected
            )
        )

        when(viewModel.textAlignment) {
            Gravity.CENTER_HORIZONTAL -> binding.imageButtonTextAlignCenter
                .setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsSelected
                    )
                )
            Gravity.START -> binding.imageButtonTextAlignLeft
                .setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsSelected
                    )
                )
            Gravity.END -> binding.imageButtonTextAlignRight
                .setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsSelected
                    )
                )
        }

        binding.imageTextColor.setColorFilter(viewModel.textColor)
    }

    private fun applyTextOptions() {
        binding.editTextTitle.setTextColor(viewModel.textColor)
        binding.editTextContent.setTextColor(viewModel.textColor)

        // binding.editTextTitle.gravity = viewModel.textAlignment
        binding.editTextContent.gravity = viewModel.textAlignment

        binding.editTextTitle.typeface = viewModel.textFont
        binding.editTextContent.typeface = viewModel.textFont

        binding.editTextTitle.textSize = viewModel.textSize
        binding.editTextContent.textSize = viewModel.textSize

        if (viewModel.textStyleBold && viewModel.textStyleItalic) {
            binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.BOLD_ITALIC)
            binding.editTextContent.setTypeface(viewModel.textFont, Typeface.BOLD_ITALIC)
        } else if (viewModel.textStyleBold) {
            binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.BOLD)
            binding.editTextContent.setTypeface(viewModel.textFont, Typeface.BOLD)
        } else if (viewModel.textStyleItalic) {
            binding.editTextTitle.setTypeface(viewModel.textFont, Typeface.ITALIC)
            binding.editTextContent.setTypeface(viewModel.textFont, Typeface.ITALIC)
        }

        setCursorColor(viewModel.textColor)
    }

    private fun setCursorColor(@ColorInt color: Int) {
        val objectColor = ColorUtilities.lightenColor(color, 0.175F)
        var highlightColor = ColorUtilities.lightenColor(
            ColorUtilities.getComplementaryColor(color),
            0.350F
        )
        highlightColor = ColorUtilities.whiteToGrey(highlightColor)

        ColorUtilities.setCursorDrawableColor(binding.editTextTitle, objectColor)
        ColorUtilities.setCursorDrawableColor(binding.editTextContent, objectColor)
        binding.editTextTitle.highlightColor = highlightColor
        binding.editTextContent.highlightColor = highlightColor
        ColorUtilities.setCursorPointerColor(binding.editTextTitle, objectColor)
        ColorUtilities.setCursorPointerColor(binding.editTextContent, objectColor)
    }

    private fun showKeyboard(view: View, inputMethodManager: InputMethodManager) {
        inputMethodManager.showSoftInput(view, 0)
    }

    private fun hideKeyboard(view: View, inputMethodManager: InputMethodManager) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.save_diary, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> backPressed()
            R.id.save_diary -> {
                if (keyboardShown)
                    hideKeyboard(binding.root, inputMethodManager)

                when (viewModel.mode) {
                    CREATE_MODE -> {
                        if (diaryContentNotEmpty())
                            saveDiary(createDiary())
                        else
                            findNavController().popBackStack()
                    }
                    DATE_OTHER_THAN_TODAY -> {
                        if (diaryContentNotEmpty())
                            saveDiary(createDiary())
                        else
                            findNavController().popBackStack()
                    }
                    EDIT_MODE -> updateDiary()
                }
            }
            R.id.location -> {
                val intent = Intent(requireContext(), GoogleMapActivity::class.java)
                intent.putExtra(KEY_GOOGLE_MAP_PLACE, viewModel.place)
                startActivityForResult(intent, PLACE_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                MediaHelper.REQUEST_IMAGE_CAPTURE -> {
                    progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
                    @Suppress("SpellCheckingInspection")
                    viewModel.getCurrentImageUri()?.let { uri ->
                        coroutineScope.launch {
                            fileUtilities.copyFileToInternalStorage(uri)?.let { copiedUri ->
                                Timber.d("File copied: $copiedUri")
                                progressDialogFragment.dismiss()
                                addMedia(MediaModel(MediaModel.Type.PHOTO, copiedUri.toString()))
                            } ?: run {
                                progressDialogFragment.dismiss()
                                showToast(
                                    requireContext(),
                                    getString(R.string.failed_to_load_image)
                                )
                            }
                        }
                    } ?: run {
                        progressDialogFragment.dismiss()
                        showToast(requireContext(), getString(R.string.failed_to_load_image))
                    }
                }
                MediaHelper.REQUEST_IMAGE_PICK -> {
                    if (data?.clipData != null) {
                        progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
                        @Suppress("SpellCheckingInspection")
                        val timestamp: String = SimpleDateFormat(
                            "yyyyMMddHHmmss",
                            Locale.getDefault()
                        ).format(Date())
                        val totalItemCount = data.clipData?.itemCount ?: 0
                        var itemCount = 0
                        for (i in 0 until totalItemCount) {
                            data.clipData?.getItemAt(i)?.uri?.let { uri ->
                                coroutineScope.launch {
                                    fileUtilities.copyFileToInternalStorage(
                                        uri,
                                        suffix = "_${timestamp}"
                                    )?.let { copiedUri ->
                                        itemCount += 1
                                        if (itemCount >= totalItemCount)
                                            progressDialogFragment.dismiss()
                                        addMedia(
                                            MediaModel(
                                                MediaModel.Type.PHOTO,
                                                copiedUri.toString()
                                            )
                                        )
                                    } ?: run {
                                        itemCount += 1
                                        if (itemCount >= totalItemCount)
                                            progressDialogFragment.dismiss()
                                        showToast(
                                            requireContext(),
                                            getString(R.string.failed_to_load_image)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (data?.data != null) {
                        progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
                        data.data?.let { uri ->
                            coroutineScope.launch {
                                fileUtilities.copyFileToInternalStorage(uri)?.let { copiedUri ->
                                    println("FILE COPIED!!!!, $copiedUri")
                                    progressDialogFragment.dismiss()
                                    addMedia(
                                        MediaModel(
                                            MediaModel.Type.PHOTO,
                                            copiedUri.toString()
                                        )
                                    )
                                } ?: run {
                                    progressDialogFragment.dismiss()
                                    showToast(
                                        requireContext(),
                                        getString(R.string.failed_to_load_image)
                                    )
                                }
                            }
                        } ?: run {
                            progressDialogFragment.dismiss()
                            showToast(requireContext(), getString(R.string.failed_to_load_image))
                        }
                    }

                    mediaAdapter.smoothScrollToEnd()
                }
                MediaHelper.REQUEST_VIDEO_PICK -> {
                    if (data?.clipData != null) {
                        progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
                        val totalItemCount = data.clipData?.itemCount ?: 0
                        var itemCount = 0
                        for (i in 0 until totalItemCount) {
                            data.clipData?.getItemAt(i)?.uri?.let { videoUri ->
                                coroutineScope.launch {
                                    fileUtilities.copyFileToInternalStorage(videoUri)
                                        ?.let { copiedUri ->
                                            itemCount += 1
                                            if (itemCount >= totalItemCount)
                                                progressDialogFragment.dismiss()
                                            addMedia(
                                                MediaModel(
                                                    MediaHelper.MediaType.VIDEO,
                                                    uriString = copiedUri.toString()
                                                )
                                            )
                                        } ?: run {
                                        itemCount += 1
                                        if (itemCount >= totalItemCount)
                                            progressDialogFragment.dismiss()
                                        showToast(
                                            requireContext(),
                                            getString(R.string.failed_to_load_video)
                                        )
                                        Timber.e("File copy failed.")
                                    }
                                }
                            } ?: run {
                                progressDialogFragment.dismiss()
                                showToast(
                                    requireContext(),
                                    getString(R.string.failed_to_load_video)
                                )
                                Timber.e("Uri is null.")
                            }
                        }
                    } else if (data?.data != null) {
                        data.data?.let { videoUri ->
                            progressDialogFragment.show(
                                requireActivity().supportFragmentManager,
                                tag
                            )
                            coroutineScope.launch {
                                fileUtilities.copyFileToInternalStorage(videoUri)
                                    ?.let { copiedUri ->
                                        progressDialogFragment.dismiss()
                                        addMedia(
                                            MediaModel(
                                                MediaHelper.MediaType.VIDEO,
                                                uriString = copiedUri.toString()
                                            )
                                        )
                                    } ?: run {
                                    progressDialogFragment.dismiss()
                                    showToast(
                                        requireContext(),
                                        getString(R.string.failed_to_load_video)
                                    )
                                    Timber.e("File copy failed.")
                                }
                            }
                        }
                    }

                    mediaAdapter.smoothScrollToEnd()
                }
                MediaHelper.REQUEST_AUDIO_PICK -> {
                    if (data?.clipData != null) {
                        progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
                        @Suppress("SpellCheckingInspection")
                        val timestamp: String = SimpleDateFormat(
                            "yyyyMMddHHmmss",
                            Locale.getDefault()
                        ).format(Date())
                        val totalItemCount = data.clipData?.itemCount ?: 0
                        var itemCount = 0
                        for (i in 0 until totalItemCount) {
                            data.clipData?.getItemAt(i)?.uri?.let { uri ->
                                coroutineScope.launch {
                                    fileUtilities.copyFileToInternalStorage(
                                        uri,
                                        suffix = "_${timestamp}"
                                    )?.let { copiedUri ->
                                        itemCount += 1
                                        if (itemCount >= totalItemCount)
                                            progressDialogFragment.dismiss()
                                        addMedia(
                                            MediaModel(
                                                MediaModel.Type.AUDIO,
                                                copiedUri.toString()
                                            )
                                        )

                                    } ?: run {
                                        itemCount += 1
                                        if (itemCount >= totalItemCount)
                                            progressDialogFragment.dismiss()
                                        showToast(
                                            requireContext(),
                                            getString(R.string.failed_to_load_audio)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (data?.data != null) {
                        progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
                        data.data?.let { uri ->
                            coroutineScope.launch {
                                fileUtilities.copyFileToInternalStorage(uri)?.let { copiedUri ->
                                    progressDialogFragment.dismiss()
                                    addMedia(
                                        MediaModel(
                                            MediaModel.Type.AUDIO,
                                            copiedUri.toString()
                                        )
                                    )
                                } ?: run {
                                    progressDialogFragment.dismiss()
                                    showToast(
                                        requireContext(),
                                        getString(R.string.failed_to_load_audio)
                                    )
                                }
                            }
                        } ?: run {
                            progressDialogFragment.dismiss()
                            showToast(requireContext(), getString(R.string.failed_to_load_audio))
                        }
                    }

                    mediaAdapter.smoothScrollToEnd()
                }
                MediaHelper.REQUEST_CODE_DRAW -> {
                    val result = data?.getByteArrayExtra("bitmap")
                    val bitmap = result?.size?.let { BitmapFactory.decodeByteArray(result, 0, it) }
                    progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
                    bitmap?.let {
                        PhotoHelper.saveBitmapToFile(requireContext(), it)?.toUri()?.let { uri ->
                            progressDialogFragment.dismiss()
                            addMedia(MediaModel(MediaHelper.MediaType.PHOTO, uri.toString()))
                        } ?: run {
                            progressDialogFragment.dismiss()
                            showToast(requireContext(), getString(R.string.failed_to_load_image))
                        }
                    } ?: run {
                        progressDialogFragment.dismiss()
                        showToast(requireContext(), getString(R.string.failed_to_load_image))
                    }
                }
                PLACE_REQUEST_CODE -> {
                    val place = data?.getParcelableExtra<PlaceModel>(KEY_GOOGLE_MAP_PLACE)
                    place?.let { addPlaceChip(it) }
                }
            }
        }
    }

    private fun addMedia(media: MediaModel) {
        if (!recyclerViewMediaIsShown) {
            binding.recyclerViewMedia.crossFadeIn(200L)
            recyclerViewMediaIsShown = true
        }

        viewModel.addMedia(media)
    }

    private fun saveDiary(diary: DiaryModel) {
        if (keyboardShown)
            hideKeyboard(binding.root, inputMethodManager)
        (requireActivity() as MainActivity).saveDiary(diary, viewModel.folder)
        findNavController().popBackStack()
    }

    private fun updateDiary() {
        if (isChanged()) {
            if (viewModel.originDiary?.folderId != viewModel.folder?.id) {
                // The diary is added to the new folder in the update function,
                // so here you just need to delete it from the existing folder.
                viewModel.originDiary?.let { diary ->
                    val originFolderId = diary.folderId
                    coroutineScope.launch {
                        withContext(Dispatchers.Default) {
                            val originFolder = viewModel.folderDao.getFolderById(originFolderId)
                            originFolder?.let { folder ->
                                (requireActivity() as MainActivity).viewModel
                                    .removeDiaryFromFolder(diary.id, folder)
                            }
                        }
                    }
                }
            }

            viewModel.originDiary?.let { diary ->
                diary.title = binding.editTextTitle.text.toString()
                diary.content = binding.editTextContent.text.toString()
                diary.textOptions = createTextOptions()
                diary.mediaArray = mediaAdapter.getMediaArray()
                diary.weatherIconIndex = viewModel.weatherIconIndex
                diary.hashTags = viewModel.selectedHashTags.toTypedArray()
                diary.folderId = viewModel.folder?.id ?: DEFAULT_FOLDER_ID
                diary.place = viewModel.place ?: createNoPlace()
            } ?: run {
                showToast(requireContext(), "원본 다이어리가 손상되었습니다.") // TODO change to resource
                saveDiary(createDiary())
            }

            (requireActivity() as MainActivity).updateDiary(viewModel.originDiary!!, viewModel.folder)
        }

        findNavController().popBackStack()
    }

    private fun isChanged(): Boolean {
        if (viewModel.originDiary == null) {
            if (binding.editTextTitle.text.isNotBlank() || binding.editTextContent.text.isNotBlank())
                return true

            if (mediaAdapter.getMediaArray().isNotEmpty())
                return true

            return false
        } else {
            val originDiary = viewModel.originDiary!!

            if (originDiary.title != binding.editTextTitle.text.toString() ||
                    originDiary.content != binding.editTextContent.text.toString() ||
                    originDiary.weatherIconIndex != viewModel.weatherIconIndex)
                return true

            if (!originDiary.mediaArray.contentEquals(mediaAdapter.getMediaArray()))
                return true

            if (originDiary.textOptions != createTextOptions())
                return true

            if (!originDiary.hashTags.contentEquals(viewModel.selectedHashTags.toTypedArray()))
                return true

            val currentFolderId = viewModel.folder?.id ?: DEFAULT_FOLDER_ID
            if (originDiary.folderId != currentFolderId)
                return true

            return false
        }
    }

    private fun createDiary(): DiaryModel {
        val title = binding.editTextTitle.text.toString()
        val content = binding.editTextContent.text.toString()

        return DiaryModel(
            title = title, content = content,
            time = viewModel.time,
            mediaArray = mediaAdapter.getMediaArray(),
            textOptions = createTextOptions(),
            liked = false,
            weatherIconIndex = viewModel.weatherIconIndex,
            hashTags = viewModel.selectedHashTags.toTypedArray(),
            folderId = viewModel.folder?.id ?: DEFAULT_FOLDER_ID,
            place = viewModel.place ?: createNoPlace()
        )
    }

    private fun createTextOptions() = TextOptionsModel(
        textAlignment = viewModel.textAlignment,
        textColor = viewModel.textColor,
        textFontId = viewModel.textFontId,
        textSize = viewModel.textSize,
        textStyleBold = viewModel.textStyleBold,
        textStyleItalic = viewModel.textStyleItalic
    )

    private fun createNoPlace() = PlaceModel(false, "", 0.0, 0.0)

    private fun showOptionItems(view: View) {
        binding.layoutPhotoOptionItems.visibility = View.GONE
        binding.layoutVideoOptionItems.visibility = View.GONE
        binding.layoutAudioOptionItems.visibility = View.GONE
        binding.layoutTextOptionItems.visibility = View.GONE

        binding.imagePhoto.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextOptionItemsUnselected
            )
        )
        binding.imageVideo.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextOptionItemsUnselected
            )
        )
        binding.imageAudio.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextOptionItemsUnselected
            )
        )
        binding.imageText.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextOptionItemsUnselected
            )
        )

        if (!layoutOptionItemsIsShown) {
            binding.layoutOptionsContainer.translateUp(
                shortAnimationDuration,
                layoutOptionItemsHeight
            )

            binding.optionItemsBackground.showUp(
                shortAnimationDuration,
                layoutOptionItemsHeight
            )

            binding.layoutOptionItems.showUp(
                mediumAnimationDuration,
                layoutOptionItemsHeight
            )
            layoutOptionItemsIsShown = true
        }

        when(view.id) {
            R.id.image_photo -> {
                binding.layoutPhotoOptionItems.visibility = View.VISIBLE
                binding.imagePhoto.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsSelected
                    )
                )
            }
            R.id.image_video -> {
                binding.layoutVideoOptionItems.visibility = View.VISIBLE
                binding.imageVideo.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsSelected
                    )
                )
            }
            R.id.image_audio -> {
                binding.layoutAudioOptionItems.visibility = View.VISIBLE
                binding.imageAudio.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsSelected
                    )
                )
            }
            R.id.image_text -> {
                binding.layoutTextOptionItems.visibility = View.VISIBLE
                binding.imageText.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextOptionItemsSelected
                    )
                )
            }
        }
    }

    private fun navigateToPhotoEditorFragment(uri: Uri) {
        if (layoutOptionItemsIsShown) {
            binding.layoutOptionItems
                .hideDownWithFading(
                    shortAnimationDuration,
                    layoutOptionItemsHeight
                )

            layoutOptionItemsIsShown = false
        }

        findNavController().navigate(
            DiaryWritingFragmentDirections.actionDiaryWritingFragmentToPhotoEditorFragment(uri)
        )
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_text_color))
            .setPreferenceName(PREFERENCE_COLOR_PICKER_DIALOG)
            .setPositiveButton(getString(R.string.ok),
                ColorEnvelopeListener { envelope, _ ->
                    val color = envelope.color
                    binding.imageTextColor.setColorFilter(
                        color,
                        PorterDuff.Mode.SRC_IN
                    )
                    binding.editTextTitle.setTextColor(color)
                    binding.editTextContent.setTextColor(color)
                    setCursorColor(color)
                    viewModel.textColor = color
                }
            )
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    private fun startExoPlayerActivity(uri: Uri) {
        startActivity(Intent(requireContext(), ExoPlayerActivity::class.java).apply {
            putExtra(EXTRA_MEDIA_URI, uri.toString())
        })
    }

    private fun showHashTagInputDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        alertDialog.setMessage("해시태그를 입력하세요.") // TODO change string resource.

        val editText = EditText(requireContext())
        val id = getCurrentTime().toInt()
        editText.id = id
        editText.onFocusChangeListener = editTextOnFocusChangeListener

        alertDialog.setView(editText)

        alertDialog.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            if (editText.text.isNotBlank()) {
                val hashTag = "#${editText.text}"
                viewModel.storeHashTagToPreferences(hashTag)
                addHashTagChip(hashTag)
            }
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            dialog.dismiss()
        }

        alertDialog.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            dialog.dismiss()
        }

        val dialog = alertDialog.show()

        val alertTitleId = resources.getIdentifier("alertTitle", "id", requireContext().packageName)
        val okButton = dialog.findViewById<Button>(android.R.id.button1)!!
        val cancelButton = dialog.findViewById<Button>(android.R.id.button2)!!
        val inflatedEditText = dialog.findViewById<EditText>(id)!!

        if (alertTitleId > 0) {
            val color = ContextCompat.getColor(requireContext(), R.color.colorTrueBlue)
            okButton.setTextColor(color)
            cancelButton.setTextColor(color)

            val layoutParams = inflatedEditText.layoutParams as FrameLayout.LayoutParams
            val dp16 = convertDpToPx(
                requireContext(),
                resources.getDimension(R.dimen.spacing_large) / resources.displayMetrics.density
            )
            layoutParams.marginEnd = dp16.toInt()
            layoutParams.marginStart = dp16.toInt()
        }
    }

    private fun addHashTagChip(hashTag: String) {
        if (!viewModel.selectedHashTags.contains(hashTag)) {
            val chip = Chip(requireContext())
            chip.text = hashTag
            chip.setTextAppearanceResource(R.style.ChipFontStyle)
            chip.setOnCloseIconClickListener {
                binding.chipGroup.removeView(it)
                viewModel.selectedHashTags.remove((it as Chip).text)
            }

            viewModel.selectedHashTags.add(hashTag)

            binding.chipGroup.addView(chip)
        }
    }

    private fun addFolderChip(folder: FolderModel) {
        viewModel.folder?.let {
            val chip = binding.chipGroup.getChildAt(0) as? Chip
            chip?.text = folder.name
            chip?.invalidate()
        } ?: run {
            val chip = Chip(requireContext())
            chip.text = folder.name
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_folder_16)
            chip.chipIconSize = convertDpToPx(requireContext(), 20F)

            chip.setChipIconTintResource(R.color.icon_color)
            chip.setTextAppearanceResource(R.style.ChipFontStyle)
            chip.setOnCloseIconClickListener {
                binding.chipGroup.removeView(it)
                viewModel.folder = null
            }

            binding.chipGroup.addView(chip, 0)
        }

        viewModel.folder = folder
    }

    private fun showConfirmDateDialog(time: Long) {
        val title = getString(R.string.confirm_date_title)
        val message = getString(R.string.confirm_date_message)
        val confirmDateDialogFragment = OkCancelDialogFragment()
            .apply {
            setDialogParameters(title, message) {
                if (time != -1L) {
                    viewModel.time = time
                    binding.textDate.text = viewModel.time.toDateFormat(getString(R.string.date_format))
                    binding.textTime.text = viewModel.time.toDateFormat(getString(R.string.time_format))
                }
            }
        }

        confirmDateDialogFragment.show(requireActivity().supportFragmentManager, confirmDateDialogFragment.tag)
    }

    private fun diaryContentNotEmpty(): Boolean {
        if (binding.editTextTitle.text.isNotBlank())
            return true

        if (binding.editTextContent.text.isNotBlank())
            return true

        if (viewModel.mediaArrayListSize > 0)
            return true

        return false
    }

    /** Add Place Chip */
    private fun addPlaceChip(place: PlaceModel) {
        viewModel.place?.let { _ ->
            val chip = binding.locationChipGroup.getChildAt(0) as? Chip
            chip?.text = place.name
            chip?.invalidate()
        } ?: run {
            val chip = Chip(requireContext())
            chip.text = place.name
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_location_on_24)
            chip.chipIconSize = convertDpToPx(requireContext(), 20F)

            chip.setChipIconTintResource(R.color.icon_color)
            chip.setTextAppearanceResource(R.style.ChipFontStyle)
            chip.setOnCloseIconClickListener {
                binding.locationChipGroup.removeView(it)
                viewModel.place = null
            }

            binding.locationChipGroup.addView(chip)
        }

        viewModel.place = place
    }

    private fun applyPrimaryThemeColor(vararg views: View) {
        for (view in views) {
            view.setBackgroundColor(MainActivity.themeColorPrimary)
            view.invalidate()
        }
    }

    private fun applySecondaryThemeColor(vararg views: View) {
        for (view in views) {
            view.setBackgroundColor(MainActivity.themeColorSecondary)
            view.invalidate()
        }
    }

    companion object {
        const val EXTRA_MEDIA_URI = "extra_media_uri"
        const val PREFERENCE_COLOR_PICKER_DIALOG = "photo_diary_preference_color_picker_dialog"
    }
}
