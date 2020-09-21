package com.duke.elliot.kim.kotlin.photodiary.diary

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDiaryWritingBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.MediaHelper
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


class DiaryWritingFragment: Fragment() {

    private lateinit var binding: FragmentDiaryWritingBinding
    private lateinit var viewModel: DiaryWritingViewModel
    private lateinit var viewModelFactory: DiaryWritingViewModelFactory
    private var layoutOptionsHeight = 0F
    private var layoutOptionsIsShown = true
    private var layoutOptionsMenuHeight = 0F
    private var layoutOptionsMenuIsShown = false
    private var mediumAnimationDuration = 0
    private var recyclerViewMediaIsShown = false
    private var shortAnimationDuration = 0
    private val bottomNavigationViewOptionsClickListener = View.OnClickListener { view ->
        showOptionsMenu(view)
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

        // val scoreFragmentArgs by navArgs<ScoreFragmentArgs>() TODO 여기서 다이어리 정보 전달 받을 것. 팩토리로전달.
        viewModelFactory = DiaryWritingViewModelFactory(null) // null 나중에 대체되야함.
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryWritingViewModel::class.java]
        viewModel.setFragment(this)

        binding.diaryWritingViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.action = DiaryWritingViewModel.Action.UNINITIALIZED
        if (viewModel.mediaArrayListSize > 0)
            binding.recyclerViewMedia.visibility = View.VISIBLE
        viewModel.mediaArrayList.observe(viewLifecycleOwner, { mediaArrayList ->
            when (viewModel.action) {
                DiaryWritingViewModel.Action.UNINITIALIZED -> {
                    binding.recyclerViewMedia.apply {
                        adapter = MediaRecyclerViewAdapter(R.layout.item_media, mediaArrayList)
                        layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                            orientation = LinearLayoutManager.HORIZONTAL
                        }
                    }
                    viewModel.action = DiaryWritingViewModel.Action.INITIALIZED
                }
                DiaryWritingViewModel.Action.ADDED -> {
                    binding.recyclerViewMedia.adapter?.notifyItemInserted(viewModel.mediaArrayListSize - 1)
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
        layoutOptionsMenuHeight = convertDpToPx(
            requireContext(),
            resources.getDimension(R.dimen.dimen_layout_options_menu_height) / resources.displayMetrics.density
        )

        binding.frameLayoutDropdown.setOnClickListener {
            when {
                layoutOptionsMenuIsShown -> {
                    binding.layoutOptionsMenu
                        .hideDown(shortAnimationDuration, layoutOptionsMenuHeight)
                    binding.optionsMenuBackground
                        .hideDown(mediumAnimationDuration, layoutOptionsMenuHeight)
                    binding.layoutOptionsContainer
                        .translateDown(mediumAnimationDuration, layoutOptionsMenuHeight)
                    layoutOptionsMenuIsShown = false
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

        binding.imagePhoto.setOnClickListener(bottomNavigationViewOptionsClickListener)
        binding.imageVideo.setOnClickListener(bottomNavigationViewOptionsClickListener)
        binding.imageAudio.setOnClickListener(bottomNavigationViewOptionsClickListener)
        binding.imageDrawing.setOnClickListener(bottomNavigationViewOptionsClickListener)

        setEventListener(
            requireActivity(),
            binding.lifecycleOwner ?: viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen && layoutOptionsMenuIsShown) {
                        binding.layoutOptionsMenu
                            .hideDownWithFading(
                                shortAnimationDuration shr 1,
                                layoutOptionsMenuHeight
                            )
                        binding.optionsMenuBackground
                            .hideDown(mediumAnimationDuration shr 1, layoutOptionsMenuHeight)
                        binding.layoutOptionsContainer
                            .translateDown(mediumAnimationDuration shr 1, layoutOptionsMenuHeight)
                        layoutOptionsMenuIsShown = false
                    }
                }
            })

        return binding.root
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
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
                    val bitmap =
                        viewModel.getCurrentPhotoBitmap()?.setConfigure(Bitmap.Config.ARGB_8888)
                    bitmap?.let { MediaModel(MediaModel.Type.PHOTO, it) }?.let { addMedia(it) }
                        ?: run {
                            showToast(requireContext(), getString(R.string.failed_to_load_image))
                        }
                }
                MediaHelper.REQUEST_IMAGE_PICK -> {
                    if (data?.clipData != null) {
                        var imageUri: Uri?
                        for (i in 0 until (data.clipData?.itemCount ?: 0)) {
                            imageUri = data.clipData?.getItemAt(i)?.uri
                            getBitmap(imageUri)?.let { MediaModel(MediaModel.Type.PHOTO, it) }
                                ?.let {
                                    addMedia(
                                        it
                                    )
                                }
                        }
                    } else if (data?.data != null) {
                        val imageUri = data.data
                        getBitmap(imageUri)?.let { MediaModel(MediaModel.Type.PHOTO, it) }?.let {
                            addMedia(
                                it
                            )
                        }
                    }
                    (binding.recyclerViewMedia.adapter as MediaRecyclerViewAdapter).smoothScrollToEnd()
                }
                MediaHelper.REQUEST_VIDEO_PICK -> {
                    if (data?.clipData != null) {
                        var videoUri: Uri?
                        for (i in 0 until (data.clipData?.itemCount ?: 0)) {
                            videoUri = data.clipData?.getItemAt(i)?.uri
                            addMedia(MediaModel(MediaHelper.MediaType.VIDEO, videoUri = videoUri))
                        }
                    } else if (data?.data != null) {
                        val videoUri = data.data
                        addMedia(MediaModel(MediaHelper.MediaType.VIDEO, videoUri = videoUri))
                    }
                }
            }
        }
    }

    private fun getBitmap(imageUri: Uri?): Bitmap? {
        try {
            imageUri?.let {
                return if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        imageUri
                    ).setConfigure(Bitmap.Config.ARGB_8888)
                } else {
                    val source =
                        ImageDecoder.createSource(
                            requireActivity().contentResolver,
                            imageUri
                        )
                    ImageDecoder.decodeBitmap(source)
                        .setConfigure(Bitmap.Config.ARGB_8888)
                }
            } ?: run {
                showToast(requireContext(), getString(R.string.image_not_found))
                return null
            }
        } catch (e: Exception) {
            showToast(requireContext(), getString(R.string.failed_to_load_image))
            e.printStackTrace()
            return null
        }
    }

    private fun addMedia(media: MediaModel) {
        if (!recyclerViewMediaIsShown)
            binding.recyclerViewMedia.visibility = View.VISIBLE

        viewModel.addMedia(media)
    }

    private fun storeDiary(diary: DiaryModel) {
        (requireActivity() as MainActivity).viewModel.add(diary)
    }

    private fun showOptionsMenu(view: View) {
        binding.layoutPhotoOptionsMenu.visibility = View.GONE
        binding.layoutVideoOptionsMenu.visibility = View.GONE
        binding.layoutAudioOptionsMenu.visibility = View.GONE

        if (!layoutOptionsMenuIsShown) {
            binding.layoutOptionsContainer.translateUp(
                shortAnimationDuration,
                layoutOptionsMenuHeight
            )

            binding.optionsMenuBackground.showUp(
                shortAnimationDuration,
                layoutOptionsMenuHeight
            )

            binding.layoutOptionsMenu.showUp(
                mediumAnimationDuration,
                layoutOptionsMenuHeight
            )
            layoutOptionsMenuIsShown = true
        }

        when(view.id) {
            R.id.image_photo -> binding.layoutPhotoOptionsMenu.visibility = View.VISIBLE
            R.id.image_video -> binding.layoutVideoOptionsMenu.visibility = View.VISIBLE
            R.id.image_audio -> binding.layoutAudioOptionsMenu.visibility = View.VISIBLE
        }
    }
}

fun Bitmap.setConfigure(config: Bitmap.Config): Bitmap = this.copy(config, true)

