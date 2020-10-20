package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDiaryViewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.TextOptionsModel
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.MediaPagerAdapter
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat

class DiaryViewFragment: Fragment() {

    private lateinit var binding: FragmentDiaryViewBinding
    private lateinit var diary: DiaryModel
    private lateinit var viewModel: DiaryViewViewModel

    fun setDiary(diary: DiaryModel) {
        this.diary = diary
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_diary_view, container, false)

        val viewModelFactory = DiaryViewViewModelFactory(diary)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryViewViewModel::class.java]

        bind(viewModel.diary)

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
}