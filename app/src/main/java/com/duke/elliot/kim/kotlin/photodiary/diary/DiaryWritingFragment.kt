package com.duke.elliot.kim.kotlin.photodiary.diary

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary.media.PhotoHelper
import kotlinx.android.synthetic.main.fragment_diary_writing.*
import kotlinx.android.synthetic.main.fragment_diary_writing.view.*

class DiaryWritingFragment: Fragment() {

    private lateinit var viewModel: DiaryWritingViewModel
    private lateinit var viewModelFactory: DiaryWritingViewModelFactory
    private var bottomNavigationViewIsShown = true
    private var bottomNavigationViewOptionsMenuIsShown = false
    private var linearLayoutOptionsHeight = 0F
    private var linearLayoutOptionsMenuHeight = 0F
    private var mediumAnimationDuration = 0
    private var recyclerViewMediaIsShown = false
    private var shortAnimationDuration = 0
    private val bottomNavigationViewOptionsClickListener = View.OnClickListener { view ->
        showOptionsMenu(view)
    }
    private val bottomNavigationViewOptionsMenuClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.linear_layout_photo_shoot -> PhotoHelper.dispatchImageCaptureIntent(this)
            R.id.linear_layout_photo_album -> PhotoHelper.dispatchImagePickerIntent(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_writing, container, false)

        initializeToolbar(view.toolbar)

        // val scoreFragmentArgs by navArgs<ScoreFragmentArgs>() TODO 여기서 다이어리 정보 전달 받을 것. 팩토리로전달.
        viewModelFactory = DiaryWritingViewModelFactory(null)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryWritingViewModel::class.java]

        viewModel.mediaArrayList.observe(viewLifecycleOwner ,{ mediaArrayList ->
            when (viewModel.action) {
                DiaryWritingViewModel.Action.UNINITIALIZED -> {
                    view.recycler_view_media.apply {
                        adapter = MediaRecyclerViewAdapter(R.layout.item_media, mediaArrayList)
                        layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                            orientation = LinearLayoutManager.HORIZONTAL
                        }
                    }
                    viewModel.action = DiaryWritingViewModel.Action.INITIALIZED
                }
                DiaryWritingViewModel.Action.ADDED -> {
                    view.recycler_view_media.adapter?.notifyItemInserted(viewModel.mediaArrayListSize - 1)
                }
            }
        })

        view.recycler_view_media.setBackgroundColor(
            ContextCompat.getColor(requireContext(),
                R.color.colorMediaRecyclerViewBackground)
        )

        mediumAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        linearLayoutOptionsHeight = convertDpToPx(requireContext(),
            resources.getDimension(R.dimen.dimen_linear_layout_options_height) / resources.displayMetrics.density)
        linearLayoutOptionsMenuHeight = convertDpToPx(requireContext(),
            resources.getDimension(R.dimen.dimen_linear_layout_options_menu_height) / resources.displayMetrics.density)

        view.frame_layout_dropdown.setOnClickListener {
            when {
                bottomNavigationViewOptionsMenuIsShown -> {
                    view.linear_layout_options_menu
                        .hideDown(shortAnimationDuration, linearLayoutOptionsMenuHeight)
                    linear_layout_options_container
                        .translateDown(mediumAnimationDuration, linearLayoutOptionsMenuHeight)
                    bottomNavigationViewOptionsMenuIsShown = false
                }
                bottomNavigationViewIsShown -> {
                    view.image_dropdown.rotate(180F, shortAnimationDuration)
                    view.linear_layout_options_container
                        .hideDown(shortAnimationDuration, linearLayoutOptionsHeight)
                    bottomNavigationViewIsShown = false
                }
                else -> {
                    view.image_dropdown.rotate(0F, shortAnimationDuration)
                    view.linear_layout_options_container
                        .showUp(shortAnimationDuration, linearLayoutOptionsHeight)
                    bottomNavigationViewIsShown = true
                }
            }
        }

        view.image_photo.setOnClickListener(bottomNavigationViewOptionsClickListener)
        view.image_video.setOnClickListener(bottomNavigationViewOptionsClickListener)
        view.image_audio.setOnClickListener(bottomNavigationViewOptionsClickListener)
        view.image_drawing.setOnClickListener(bottomNavigationViewOptionsClickListener)

        view.linear_layout_photo_shoot.setOnClickListener(bottomNavigationViewOptionsMenuClickListener)
        view.linear_layout_photo_album.setOnClickListener(bottomNavigationViewOptionsMenuClickListener)

        return view
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                storeDiary(DiaryModel(date="b",title = "B",content="b",time=0L))
                requireActivity().onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                PhotoHelper.REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    addMedia(MediaModel(bitmap))
                }
                PhotoHelper.REQUEST_IMAGE_PICK -> {

                }
            }
        }
    }

    private fun addMedia(media: MediaModel) {
        if (!recyclerViewMediaIsShown)
            recycler_view_media.visibility = View.VISIBLE

        viewModel.addMedia(media)
    }

    private fun storeDiary(diary: DiaryModel) {
        (requireActivity() as MainActivity).viewModel.add(diary)
    }

    private fun showOptionsMenu(view: View) {
        linear_layout_photo_options_menu.visibility = View.GONE
        linear_layout_video_options_menu.visibility = View.GONE
        linear_layout_audio_options_menu.visibility = View.GONE
        bottomNavigationViewOptionsMenuIsShown = true

        linear_layout_options_container.translateUp(shortAnimationDuration, linearLayoutOptionsMenuHeight)

        when(view.id) {
            R.id.image_photo -> {
                linear_layout_options_menu.showUp(mediumAnimationDuration, linearLayoutOptionsMenuHeight)
                linear_layout_photo_options_menu.visibility = View.VISIBLE
            }
        }
    }
}