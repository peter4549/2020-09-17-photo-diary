package com.duke.elliot.kim.kotlin.photodiary.export

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPdfPreviewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import com.google.android.material.chip.Chip


class PdfPreviewFragment: Fragment() {

    private lateinit var binding: FragmentPdfPreviewBinding
    private lateinit var viewModel: PdfPreviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pdf_preview, container, false)

        val pdfPreviewFragmentArgs by navArgs<PdfPreviewFragmentArgs>()

        val viewModelFactory = PdfPreviewViewModelFactory(pdfPreviewFragmentArgs.diary!!)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[PdfPreviewViewModel::class.java]

        initializeView()

        return binding.root
    }

    private fun initializeView() {
        val diary = viewModel.diary
        binding.textDate.text = diary.time.toDateFormat(getString(R.string.date_format))
        binding.textTime.text = diary.time.toDateFormat(getString(R.string.time_format))
        setImage(binding.imageWeather, DiaryWritingViewModel.weatherIconIds[diary.weatherIconIndex])

        binding.editTextTitle.setText(diary.title)

        for (hashTag in diary.hashTags) {
            val chip = Chip(binding.chipGroup.context)
            chip.text = hashTag
            chip.setTextAppearanceResource(R.style.ChipFontStyle)
            chip.isCloseIconVisible = false
            binding.chipGroup.addView(chip)
        }

        if (diary.mediaArray.isEmpty())
            binding.imageView0.visibility = View.GONE

        binding.editTextContent.setText(diary.content)

        val photoArray = diary.mediaArray.filter { it.type == MediaHelper.MediaType.PHOTO }

        for ((index, photo) in photoArray.withIndex()) {
            if (index == 0) {
                binding.imageView0.visibility = View.VISIBLE
                setImage(binding.imageView0, diary.mediaArray[0].uriString)
            } else {
                val imageView = ImageView(requireContext())
                imageView.layoutParams = binding.imageView0.layoutParams
                (imageView.layoutParams as LinearLayout.LayoutParams).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                val editText = EditText(requireContext())
                editText.layoutParams = binding.editTextContent.layoutParams
                (editText.layoutParams as LinearLayout.LayoutParams).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                editText.setText("ABC")

                binding.container.addView(imageView, imageView.layoutParams)

                binding.container.addView(editText, editText.layoutParams)
                editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_white_rounded_corners)
                setImage(imageView, photo.uriString)
            }
        }
    }
}