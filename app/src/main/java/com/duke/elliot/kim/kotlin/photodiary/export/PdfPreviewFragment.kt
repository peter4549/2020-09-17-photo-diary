package com.duke.elliot.kim.kotlin.photodiary.export

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPdfPreviewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_pdf_page.view.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class PdfPreviewFragment: Fragment() {

    private lateinit var binding: FragmentPdfPreviewBinding
    private lateinit var mediaScanner: MediaScanner
    private lateinit var viewModel: PdfPreviewViewModel
    private val pdfPages: MutableList<LinearLayout> = mutableListOf()
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
        mediaScanner = MediaScanner.newInstance(requireContext())

        binding.exportPdf.setOnClickListener {
            showInputDialog(requireContext(), getString(R.string.pdf_file_name_input_message)) { text ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    createPdfFileQ(text)
                else
                    createPdfFile(text)
            }
        }

        return binding.root
    }

    private fun initializeView() {
        val diary = viewModel.diary
        val photos = diary.mediaArray.filter { it.type == MediaModel.Type.PHOTO }

        binding.textDate.text = diary.time.toDateFormat(getString(R.string.date_format))
        binding.textTime.text = diary.time.toDateFormat(getString(R.string.time_format))
        setImage(binding.imageWeather, DiaryWritingViewModel.weatherIconIds[diary.weatherIconIndex])

        binding.editTextTitle.setText(diary.title)
        binding.editTextContent.setText(diary.content)

        for (hashTag in diary.hashTags) {
            val chip = Chip(binding.chipGroup.context)
            chip.text = hashTag
            chip.setTextAppearanceResource(R.style.ChipFontStyle)
            chip.isCloseIconVisible = false
            binding.chipGroup.addView(chip)
        }

        pdfPages.add(binding.firstLayout)

        if (photos.isNotEmpty()) {
            val firstPhoto = photos[0]
            setImage(binding.imagePhoto, firstPhoto.uriString)
        } else
            binding.imagePhoto.visibility = View.GONE

        if (photos.count() > 1) {
            val photoSublist = photos.subList(1, photos.count())

            for (photo in photoSublist) {
                val linearLayout = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_pdf_page, binding.container, false) as LinearLayout

                setImage(linearLayout.imagePhoto, photo.uriString)

                binding.container.addView(linearLayout)
                pdfPages.add(linearLayout)
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

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background: Drawable? = view.background

        if (background != null)
            background.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)

        view.draw(canvas)

        return bitmap
    }

    private fun createPdfFile(fileName: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            try {
                val path = getDocumentDirectory()
                val directory = File(path, getString(R.string.app_name))
                if (!directory.exists())
                    directory.mkdir()

                val file = File(directory, "/${fileName}.pdf")
                file.createNewFile()
                val fileOutputStream = FileOutputStream(file)

                for ((index, pageView) in pdfPages.withIndex()) {
                    val pageInfo = PdfDocument.PageInfo
                        .Builder(pageView.width, pageView.height, 1 + index).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val bitmap = getBitmapFromView(pageView)
                    val canvas = page.canvas

                    bitmap?.let { canvas.drawBitmap(it, 0F, 0F, null) } ?: throw NullPointerException()
                    pdfDocument.finishPage(page)
                }

                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                fileOutputStream.close()
                mediaScanner.scanMedia(file.absolutePath)

                showToast(
                    requireContext(),
                    getString(R.string.pdf_file_created) + file.absolutePath
                )
            } catch (e: Exception) {
                showToast(requireContext(), getString(R.string.failed_to_create_pdf_file))
                Timber.e(e)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun createPdfFileQ(fileName: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            try {
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS.toString() + "/${
                        getString(
                            R.string.app_name
                        )
                    }/"
                )
                val uri = requireContext().contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    values
                )
                val outputStream = uri?.let { requireContext().contentResolver.openOutputStream(it) }

                for ((index, pageView) in pdfPages.withIndex()) {
                    val pageInfo = PdfDocument.PageInfo
                        .Builder(pageView.width, pageView.height, 1 + index).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val bitmap = getBitmapFromView(pageView)
                    val canvas = page.canvas

                    bitmap?.let { canvas.drawBitmap(it, 0F, 0F, null) } ?: throw NullPointerException()
                    pdfDocument.finishPage(page)
                }

                pdfDocument.writeTo(outputStream)
                pdfDocument.close()
                outputStream?.close()
                showToast(requireContext(), getString(R.string.pdf_file_created))
                mediaScanner.scanMedia(uri?.path ?: "")
            } catch (e: Exception) {
                showToast(requireContext(), getString(R.string.failed_to_create_pdf_file))
                Timber.e(e)
            }
        }
    }
}