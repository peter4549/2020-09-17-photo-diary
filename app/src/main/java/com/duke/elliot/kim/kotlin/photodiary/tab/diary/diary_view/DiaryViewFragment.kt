package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDiaryViewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingFragment
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.TextOptionsModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.ExoPlayerActivity
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.MediaPagerAdapter
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import com.google.android.material.chip.Chip

class DiaryViewFragment: Fragment() {

    private lateinit var binding: FragmentDiaryViewBinding
    private lateinit var diary: DiaryModel
    private lateinit var viewModel: DiaryViewViewModel
    private lateinit var mediaPagerAdapter: MediaPagerAdapter

    fun setDiary(diary: DiaryModel) {
        this.diary = diary
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_diary_view, container, false)

        val viewModelFactory = DiaryViewViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryViewViewModel::class.java]

        if (!viewModel.initialized) {
            viewModel.setDiary(diary)
            viewModel.initialized = true
        }

        bind(viewModel.getDiary())

        mediaPagerAdapter = MediaPagerAdapter().apply {
            setContext(requireContext())
            setMediaList(viewModel.getMediaList())
        }

        return binding.root
    }

    private fun bind(diary: DiaryModel) {
        binding.textDate.text = diary.time.toDateFormat(getString(R.string.date_format))
        binding.textTime.text = diary.time.toDateFormat(getString(R.string.time_format))

        binding.textTitle.text = diary.title
        binding.textContent.text = diary.content

        if (diary.mediaArray.isEmpty())
            binding.mediaContainer.visibility = View.GONE
        else {
            binding.viewPager.adapter = MediaPagerAdapter().apply {
                setContext(requireContext())
                setMediaList(diary.mediaArray.toList())
            }

            binding.viewPager.setSingleTapUpListener {
                mediaPagerAdapter.getItem(binding.viewPager.currentItem)?.let {
                    when (it.type) {
                        MediaHelper.MediaType.PHOTO -> navigateToPhotoViewerFragment(it.uriString)
                        MediaHelper.MediaType.VIDEO -> startExoPlayerActivity(it.uriString)
                        MediaHelper.MediaType.AUDIO -> startExoPlayerActivity(it.uriString)
                    }
                }
            }
        }

        for (hashTag in diary.hashTags) {
            val chip = Chip(binding.chipGroup.context)
            chip.text = hashTag
            chip.setTextAppearanceResource(R.style.ChipFontStyle)
            chip.isCloseIconVisible = false
            binding.chipGroup.addView(chip)
        }

        applyTextOptions(diary.textOptions)
    }

    private fun applyTextOptions(textOptions: TextOptionsModel) {
        val font = getFont(requireContext(), textOptions.textFontId)

        binding.textTitle.setTextColor(textOptions.textColor)
        binding.textContent.setTextColor(textOptions.textColor)

        binding.textTitle.typeface = font
        binding.textContent.typeface = font

        if (textOptions.textStyleBold && textOptions.textStyleItalic)
            binding.textContent.setTypeface(font, Typeface.BOLD_ITALIC)
        else if (textOptions.textStyleBold)
            binding.textContent.setTypeface(font, Typeface.BOLD)
        else if (textOptions.textStyleItalic)
            binding.textContent.setTypeface(font, Typeface.ITALIC)

        binding.textContent.gravity = textOptions.textAlignment
    }

    private fun startExoPlayerActivity(uriString: String) {
        startActivity(Intent(requireContext(), ExoPlayerActivity::class.java).apply {
            putExtra(DiaryWritingFragment.EXTRA_MEDIA_URI, uriString)
        })
    }

    private fun navigateToPhotoViewerFragment(uriString: String) {
        findNavController().navigate(DiaryViewPagerFragmentDirections
            .actionDiaryViewPagerFragmentToPhotoViewerFragment(uriString))
    }
}