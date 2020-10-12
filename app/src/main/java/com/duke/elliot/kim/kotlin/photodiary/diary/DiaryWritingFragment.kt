package com.duke.elliot.kim.kotlin.photodiary.diary

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDiaryWritingBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.VideoPlayerActivity
import com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor.PhotoEditorFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.text.SimpleDateFormat
import java.util.*

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
    private var mediumAnimationDuration = 0
    private var recyclerViewMediaIsShown = false
    private var shortAnimationDuration = 0

    private val optionsOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.image_drawing -> MediaHelper.startDrawingActivity(this)
            else -> {
                if (!keyboardShown)
                    showOptionsMenu(view)
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
            R.id.image_audio_library_item -> MediaHelper.audioHelper.dispatchAudioContentPickerIntent(this)
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

        progressDialogFragment = ProgressDialogFragment.instance

        // val scoreFragmentArgs by navArgs<ScoreFragmentArgs>() TODO 여기서 다이어리 정보 전달 받을 것. 팩토리로전달.
        viewModelFactory = DiaryWritingViewModelFactory(null) // null 나중에 대체되야함.
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryWritingViewModel::class.java]

        fileUtilities = FileUtilities(requireContext())
        initializeSpinners()
        inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hideKeyboard(binding.root, inputMethodManager)

        binding.textDate.text = viewModel.date
        binding.textTime.text = viewModel.time

        binding.diaryWritingViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.action = DiaryWritingViewModel.Action.UNINITIALIZED
        if (viewModel.mediaArrayListSize > 0)
            binding.recyclerViewMedia.visibility = View.VISIBLE
        viewModel.mediaArrayList.observe(viewLifecycleOwner, { mediaArrayList ->
            when (viewModel.action) {
                DiaryWritingViewModel.Action.UNINITIALIZED -> {
                    mediaAdapter = MediaAdapter(R.layout.item_media, mediaArrayList).apply {
                        setItemClickListener {
                            viewModel.selectedItemPosition = getSelectedPosition()
                            when(it.type) {
                                MediaHelper.MediaType.PHOTO -> navigateToPhotoEditorFragment(it.uri)
                                MediaHelper.MediaType.VIDEO -> startVideoPlayerActivity(it.uri)
                                MediaHelper.MediaType.AUDIO -> { /* play audio */ }
                                else -> {
                                    // TODO: Throw class cast exception
                                }
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

        initializeOptionItems()

        setEventListener(
            requireActivity(),
            binding.lifecycleOwner ?: viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (!isOpen) {
                        binding.editTextTitle.clearFocus()
                        binding.editTextContent.clearFocus()
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

        return binding.root
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
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

    private fun initializeSpinners() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.fonts,
            R.layout.item_spinner
        ).also { adapter ->
            binding.spinnerFont.adapter = adapter
        }

        val fontSizes = arrayOf(
            12, 14, 16, 18, 20, 22, 24
        )
        binding.spinnerTextSize.adapter =
            ArrayAdapter(requireContext(), R.layout.item_spinner, fontSizes)
    }

    private fun showKeyboard(view: View, inputMethodManager: InputMethodManager) {
        inputMethodManager.showSoftInput(view, 0)
    }

    private fun hideKeyboard(view: View, inputMethodManager: InputMethodManager) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                // TODO save data and back. (ask about edit state...)
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
                        CoroutineScope(Dispatchers.Main).launch {
                            fileUtilities.copyFileToInternalStorage(uri)?.let { copiedUri ->
                                progressDialogFragment.dismiss()
                                addMedia(MediaModel(MediaModel.Type.PHOTO, copiedUri))
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
                        val timestamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                        val totalItemCount = data.clipData?.itemCount ?: 0
                        var itemCount = 0
                        for (i in 0 until totalItemCount) {
                            data.clipData?.getItemAt(i)?.uri?.let { uri ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    fileUtilities.copyFileToInternalStorage(
                                        uri,
                                        suffix = "_${timestamp}"
                                    )?.let { copiedUri ->
                                        itemCount += 1
                                        if (itemCount >= totalItemCount)
                                            progressDialogFragment.dismiss()
                                        addMedia(MediaModel(MediaModel.Type.PHOTO, copiedUri))
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
                            CoroutineScope(Dispatchers.Main).launch {
                                fileUtilities.copyFileToInternalStorage(uri)?.let { copiedUri ->
                                    progressDialogFragment.dismiss()
                                    addMedia(MediaModel(MediaModel.Type.PHOTO, copiedUri))
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
                                CoroutineScope(Dispatchers.Main).launch {
                                    fileUtilities.copyFileToInternalStorage(videoUri)
                                        ?.let { copiedUri ->
                                            itemCount += 1
                                            if (itemCount >= totalItemCount)
                                                progressDialogFragment.dismiss()
                                            addMedia(
                                                MediaModel(
                                                    MediaHelper.MediaType.VIDEO,
                                                    uri = copiedUri
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
                                    }
                                }
                            } ?: run {
                                // TODO: Change Message Image -> VIDEO
                                progressDialogFragment.dismiss()
                                showToast(requireContext(), getString(R.string.failed_to_load_video))
                            }
                        }
                    } else if (data?.data != null) {
                        data.data?.let { videoUri ->
                            progressDialogFragment.show(
                                requireActivity().supportFragmentManager,
                                tag
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                fileUtilities.copyFileToInternalStorage(videoUri)
                                    ?.let { copiedUri ->
                                        progressDialogFragment.dismiss()
                                        addMedia(
                                            MediaModel(
                                                MediaHelper.MediaType.VIDEO,
                                                uri = copiedUri
                                            )
                                        )
                                    } ?: run {
                                    progressDialogFragment.dismiss()
                                    showToast(
                                        requireContext(),
                                        getString(R.string.failed_to_load_video)
                                    )
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
                        val timestamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                        val totalItemCount = data.clipData?.itemCount ?: 0
                        var itemCount = 0
                        for (i in 0 until totalItemCount) {
                            data.clipData?.getItemAt(i)?.uri?.let { uri ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    fileUtilities.copyFileToInternalStorage(
                                        uri,
                                        suffix = "_${timestamp}"
                                    )?.let { copiedUri ->
                                        itemCount += 1
                                        if (itemCount >= totalItemCount)
                                            progressDialogFragment.dismiss()
                                        addMedia(MediaModel(MediaModel.Type.AUDIO, copiedUri))
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
                            CoroutineScope(Dispatchers.Main).launch {
                                fileUtilities.copyFileToInternalStorage(uri)?.let { copiedUri ->
                                    progressDialogFragment.dismiss()
                                    addMedia(MediaModel(MediaModel.Type.AUDIO, copiedUri))
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
                            addMedia(MediaModel(MediaHelper.MediaType.PHOTO, uri))
                        } ?: run {
                            progressDialogFragment.dismiss()
                            showToast(requireContext(), getString(R.string.failed_to_load_image))
                        }
                    } ?: run {
                        progressDialogFragment.dismiss()
                        showToast(requireContext(), getString(R.string.failed_to_load_image))
                    }
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

    private fun storeDiary(diary: DiaryModel) {
        (requireActivity() as MainActivity).viewModel.add(diary)
    }

    override fun onDestroy() {
        super.onDestroy()
        PhotoHelper.deleteTempJpegFiles(requireContext())
    }

    private fun showOptionsMenu(view: View) {
        binding.layoutPhotoOptionItems.visibility = View.GONE
        binding.layoutVideoOptionItems.visibility = View.GONE
        binding.layoutAudioOptionItems.visibility = View.GONE
        binding.layoutTextOptionItems.visibility = View.GONE

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
            R.id.image_photo -> binding.layoutPhotoOptionItems.visibility = View.VISIBLE
            R.id.image_video -> binding.layoutVideoOptionItems.visibility = View.VISIBLE
            R.id.image_audio -> binding.layoutAudioOptionItems.visibility = View.VISIBLE
            R.id.image_text -> binding.layoutTextOptionItems.visibility = View.VISIBLE
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

    private fun startVideoPlayerActivity(uri: Uri) {
        startActivity(Intent(requireContext(), VideoPlayerActivity::class.java).apply {
            putExtra(EXTRA_VIDEO_URI, uri.toString())
        })
    }

    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
    }
}
