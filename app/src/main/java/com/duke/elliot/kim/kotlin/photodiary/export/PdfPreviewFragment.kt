package com.duke.elliot.kim.kotlin.photodiary.export

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPdfPreviewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.convertDpToPx
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class PdfPreviewFragment: Fragment() {

    private lateinit var binding: FragmentPdfPreviewBinding
    private lateinit var viewModel: PdfPreviewViewModel
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pdf_preview, container, false)

        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        val pdfPreviewFragmentArgs by navArgs<PdfPreviewFragmentArgs>()

        val viewModelFactory = PdfPreviewViewModelFactory(pdfPreviewFragmentArgs.diary!!)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[PdfPreviewViewModel::class.java]

        initializeView()

        binding.exportPdf.setOnClickListener {
            PdfUtilities.viewToPdf(binding.imageView0, requireContext())
        }

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

        val photos = diary.mediaArray.filter { it.type == MediaHelper.MediaType.PHOTO }

        for ((index, photo) in photos.withIndex()) {
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

                val spacing = convertDpToPx(requireContext(), 8F).toInt()
                editText.setPadding(spacing, spacing, spacing, spacing)
                editText.setLineSpacing(0F, 1.1F)

                binding.container.addView(imageView, imageView.layoutParams)

                binding.container.addView(editText, editText.layoutParams)
                editText.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.background_white_rounded_corners
                )
                setImage(imageView, photo.uriString)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }

        return super.onOptionsItemSelected(item)
    }
}