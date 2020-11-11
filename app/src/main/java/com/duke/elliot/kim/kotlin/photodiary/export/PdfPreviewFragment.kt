package com.duke.elliot.kim.kotlin.photodiary.export

import android.annotation.SuppressLint
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
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPdfPreviewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
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
    private lateinit var pdfPageAdapter: MyAdapter
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
                    createPdfFile2(text)
                else
                    createPdfFile(text)
            }
        }

        return binding.root
    }

    private fun initializeView() {
        val diary = viewModel.diary
        val photos = diary.mediaArray.filter { it.type == MediaModel.Type.PHOTO }
        for (i in 0..photos.count() - 1) {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_pdf_page, binding.container, false) as LinearLayout


            setImage(view.imagePhoto, photos[i].uriString)

            binding.container.addView(view)
        }
        //pdfPageAdapter = MyAdapter(viewModel.diary)
        //binding.recyclerViewPdfPage.layoutManager =
        //    GridLayoutManagerWrapper(requireContext(), 1)
        //binding.recyclerViewPdfPage.adapter = pdfPageAdapter
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
                val path = getDocumentDirectory(requireContext())
                val directory = File(path, getString(R.string.app_name))
                if (!directory.exists())
                    directory.mkdir()

                val file = File(directory, "/${fileName}.pdf")
                file.createNewFile()
                val fileOutputStream = FileOutputStream(file)
                println("BBBB ${pdfPageAdapter.count}")
                withContext(Dispatchers.Main) {
                    pdfPageAdapter.notifyDataSetChanged()
                    //binding.recyclerViewPdfPage.layoutManager?.scrollToPosition(pdfPageAdapter.itemCount - 1)
                    //binding.recyclerViewPdfPage.layoutManager?.scrollToPosition(0)
                }

                for (i in 0 until pdfPageAdapter.count) {
                    val pageView = pdfPageAdapter.getViewByPosition(i) ?: break
                    println("AAAA $i")
                    val pageInfo = PdfDocument.PageInfo
                        .Builder(pageView.width, pageView.height, 1 + i).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val bitmap = getBitmapFromView(pageView)
                    val canvas = page.canvas

                    bitmap?.let { canvas.drawBitmap(it, 0F, 0F, null) } ?: throw NullPointerException()
                    pdfDocument.finishPage(page)
                }

                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                // fileOutputStream.close()
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
    private fun createPdfFile2(fileName: String) {
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

                println("AAAA ${pdfPageAdapter.count}")
                for (i in 0 .. pdfPageAdapter.count) {
                    val pageView = binding.recyclerViewPdfPage.getChildAt(i) ?: break
                    val pageInfo = PdfDocument.PageInfo
                        .Builder(pageView.width, pageView.height, 1 + i).create()
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
                println("zzzzz: ${uri?.path}")
                mediaScanner.scanMedia(uri?.path ?: "")
            } catch (e: Exception) {
                showToast(requireContext(), getString(R.string.failed_to_create_pdf_file))
                Timber.e(e)
            }
        }
    }

    inner class MyAdapter(private val diary: DiaryModel) :
        BaseAdapter() {

        private lateinit var firstPhoto: MediaModel
        private lateinit var photoSublist: List<MediaModel>
        private val photos = diary.mediaArray.filter { it.type == MediaHelper.MediaType.PHOTO }

        init {
            if (photos.isNotEmpty())
                firstPhoto = photos[0]

            if (photos.count() > 1)
                photoSublist = photos.subList(1, photos.count())
        }

        override fun getCount(): Int {
            return if (::photoSublist.isInitialized)
                photoSublist.count() + 1
            else
                1
        }

        override fun getItem(p0: Int): Any? {
            return null
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            println("ZIONXIAL")
            val view: View = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_pdf_page, parent, false)
            if (photos.isNotEmpty()) {

                val photo = photos[position]
                if (position == 0) {
                    bindFirstView(view)
                } else {
                    setImage(view.imagePhoto, photo.uriString)
                }
            } else
                bindFirstView(view)

            println("HHHHHH $position")
            return view
        }

        private fun bindFirstView(view: View) {
            view.date_time_container.visibility = View.VISIBLE
            view.edit_text_title.visibility = View.VISIBLE

            view.text_date.text = diary.time.toDateFormat(getString(R.string.date_format))
            view.text_time.text = diary.time.toDateFormat(getString(R.string.time_format))
            setImage(
                view.imageWeather,
                DiaryWritingViewModel.weatherIconIds[diary.weatherIconIndex]
            )

            view.edit_text_title.setText(diary.title)

            for (hashTag in diary.hashTags) {
                val chip = Chip(view.chip_group.context)
                chip.text = hashTag
                chip.setTextAppearanceResource(R.style.ChipFontStyle)
                chip.isCloseIconVisible = false
                view.chip_group.addView(chip)
            }

            if (::firstPhoto.isInitialized)
                setImage(view.imagePhoto, firstPhoto.uriString)
            else
                view.imagePhoto.visibility = View.GONE
        }

        fun getViewByPosition(pos: Int): View? {
            val firstListItemPosition: Int = binding.recyclerViewPdfPage.getFirstVisiblePosition()
            val lastListItemPosition: Int = firstListItemPosition + binding.recyclerViewPdfPage.getChildCount() - 1
            return if (pos < firstListItemPosition || pos > lastListItemPosition) {
                binding.recyclerViewPdfPage.getAdapter().getView(pos, null, binding.recyclerViewPdfPage)
            } else {
                val childIndex = pos - firstListItemPosition
                binding.recyclerViewPdfPage.getChildAt(childIndex)
            }
        }
    }

    inner class PdfPageRecyclerViewAdapter(private val diary: DiaryModel): RecyclerView.Adapter<PdfPageRecyclerViewAdapter.ViewHolder>() {
        private lateinit var firstPhoto: MediaModel
        private lateinit var photoSublist: List<MediaModel>
        private val photos = diary.mediaArray.filter { it.type == MediaHelper.MediaType.PHOTO }

        init {
            if (photos.isNotEmpty())
                firstPhoto = photos[0]

            if (photos.count() > 1)
               photoSublist = photos.subList(1, photos.count())
        }

        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(requireContext()).inflate(
                R.layout.item_pdf_page,
                parent,
                false
            )

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return if (::photoSublist.isInitialized)
                photoSublist.count() + 1
            else
                1
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (photos.isNotEmpty()) {
                val photo = photos[position]
                if (position == 0) {
                    bindFirstView(holder.view)
                } else {
                    setImage(holder.view.imagePhoto, photo.uriString)
                }
            } else
                bindFirstView(holder.view)

            println("HHHHHH $position")
        }

        private fun bindFirstView(view: View) {
            view.date_time_container.visibility = View.VISIBLE
            view.edit_text_title.visibility = View.VISIBLE

            view.text_date.text = diary.time.toDateFormat(getString(R.string.date_format))
            view.text_time.text = diary.time.toDateFormat(getString(R.string.time_format))
            setImage(
                view.imageWeather,
                DiaryWritingViewModel.weatherIconIds[diary.weatherIconIndex]
            )

            view.edit_text_title.setText(diary.title)

            for (hashTag in diary.hashTags) {
                val chip = Chip(view.chip_group.context)
                chip.text = hashTag
                chip.setTextAppearanceResource(R.style.ChipFontStyle)
                chip.isCloseIconVisible = false
                view.chip_group.addView(chip)
            }

            if (::firstPhoto.isInitialized)
                setImage(view.imagePhoto, firstPhoto.uriString)
            else
                view.imagePhoto.visibility = View.GONE
        }
    }
}