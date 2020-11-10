package com.duke.elliot.kim.kotlin.photodiary.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPdfPreviewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_pdf_page.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.NullPointerException

class PdfPreviewFragment: Fragment() {

    private lateinit var binding: FragmentPdfPreviewBinding
    private lateinit var viewModel: PdfPreviewViewModel
    private lateinit var photoSublist: List<MediaModel>
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
            showInputDialog(requireContext(), getString(R.string.pdf_file_name_input_message)) { text ->
                createPdfFile(text)
            }
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
        binding.imageView0.visibility = View.VISIBLE
        setImage(binding.imageView0, photos[0].uriString)

        if (photos.count() > 1) {
            binding.recyclerViewPdfPage.visibility = View.VISIBLE
            photoSublist = photos.subList(1, photos.count())
            binding.recyclerViewPdfPage.layoutManager =
                GridLayoutManagerWrapper(requireContext(), 1)
            binding.recyclerViewPdfPage.adapter = PdfPageRecyclerViewAdapter(photoSublist)
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
                val pageInfo = PdfDocument.PageInfo
                    .Builder(binding.container.width, binding.container.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val bitmap = getBitmapFromView(binding.container)

                val path = getOutputDirectory(requireContext()).absolutePath
                val directory = File(path, getString(R.string.app_name))
                if (!directory.exists())
                    directory.mkdir()

                val file = File(directory, "/${fileName}.pdf")
                file.createNewFile()

                val fileOutputStream = FileOutputStream(file)
                val canvas = page.canvas

                bitmap?.let { canvas.drawBitmap(it, 0F, 0F, null) } ?: throw NullPointerException()
                pdfDocument.finishPage(page)

                for ((index, _) in photoSublist.withIndex()) {
                    val itemView =
                        binding.recyclerViewPdfPage.layoutManager?.findViewByPosition(index)
                            ?: break
                    val pageInfo2 = PdfDocument.PageInfo
                        .Builder(itemView.width, itemView.height, index + 2).create()
                    val page2 = pdfDocument.startPage(pageInfo2)
                    val bitmap2 = getBitmapFromView(itemView)

                    val canvas2 = page2.canvas

                    bitmap2?.let { canvas2.drawBitmap(it, 0F, 0F, null) }
                        ?: throw NullPointerException()
                    pdfDocument.finishPage(page2)
                }

                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
            } catch (e: Exception) {
                showToast(requireContext(), getString(R.string.failed_to_create_pdf_file))
                Timber.e(e)
            }
        }
    }

    inner class PdfPageRecyclerViewAdapter(private val photos: List<MediaModel>): RecyclerView.Adapter<PdfPageRecyclerViewAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_pdf_page, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = photos.count()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val photo = photos[position]
            setImage(holder.view.imagePhoto, photo.uriString)
        }
    }
}

/*
private void createPDFWithMultipleImage(){
    File file = getOutputFile();
    if (file != null){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            PdfDocument pdfDocument = new PdfDocument();

            for (int i = 0; i < images.size(); i++){
                Bitmap bitmap = BitmapFactory.decodeFile(images.get(i).getPath());
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), (i + 1)).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                canvas.drawPaint(paint);
                canvas.drawBitmap(bitmap, 0f, 0f, null);
                pdfDocument.finishPage(page);
                bitmap.recycle();
            }
            pdfDocument.writeTo(fileOutputStream);
            pdfDocument.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

 */