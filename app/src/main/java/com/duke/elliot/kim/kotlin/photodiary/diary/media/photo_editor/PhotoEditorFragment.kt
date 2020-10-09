package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPhotoEditorBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor.TextEditorDialogFragment.TextEditor
import com.duke.elliot.kim.kotlin.photodiary.diary.media.simple_crop_view.SimpleCropViewFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.PhotoFilter
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1000

class PhotoEditorFragment: Fragment(),
    EditingToolRecyclerViewAdapter.OnItemSelected,
    BrushPropertiesBottomSheetDialogFragment.Properties,
    FilterViewAdapter.FilterListener,
    EmojiBottomSheetDialogFragment.EmojiListener,
    StickerBottomSheetDialogFragment.StickerListener {

    private lateinit var binding: FragmentPhotoEditorBinding
    private lateinit var brushPropertiesBottomSheetDialogFragment: BrushPropertiesBottomSheetDialogFragment
    private lateinit var emojiBottomSheetDialogFragment: EmojiBottomSheetDialogFragment
    private lateinit var photoEditor: PhotoEditor
    // private lateinit var stickerBottomSheetDialogFragment: StickerBottomSheetDialogFragment
    private lateinit var viewModel: PhotoEditorViewModel
    private lateinit var viewModelFactory: PhotoEditorFactory
    private val constraintSet = ConstraintSet()
    private val editingToolRecyclerViewAdapter = EditingToolRecyclerViewAdapter(this)
    private val filterViewAdapter = FilterViewAdapter(this)
    private var editedImageUri: Uri? = null
    private var isFilterVisible = false
    private var navigationDestination: NavigationDestination? = null
    private var originImageUri: Uri? = null

    private val okCancelDialogFragment = OkCancelDialogFragment().apply {
        setDialogParameters("이미지 저장", "수정된 이미지를 저장하시겠습니까?") {
            popBackStackWithSaveImage()
            this.dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_photo_editor,
            container,
            false
        )

        val photoEditorFragmentArgs by navArgs<PhotoEditorFragmentArgs>()
        var imageUri = photoEditorFragmentArgs.imageUri
        originImageUri = imageUri

        if (editedImageUri != null)
            imageUri = editedImageUri

        findNavController().currentBackStackEntry
            ?.savedStateHandle?.get<Uri>(SimpleCropViewFragment.KEY_CROPPED_BITMAP_URI)
            ?.let {
                imageUri = it
                findNavController().currentBackStackEntry?.savedStateHandle
                    ?.remove<Uri>(SimpleCropViewFragment.KEY_CROPPED_BITMAP_URI)
            }

        viewModelFactory = PhotoEditorFactory()
        viewModel = ViewModelProvider(this, viewModelFactory)[PhotoEditorViewModel::class.java]
        imageUri?.let { viewModel.updateImage(it) } ?: run {
            showToast(requireContext(), getString(R.string.failed_to_load_image))
        }

        viewModel.imageUri.observe(viewLifecycleOwner) {
            Glide.with(requireContext())
                .load(it)
                .error(R.drawable.ic_sharp_not_interested_112)
                .fallback(R.drawable.ic_sharp_not_interested_112)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        showToast(requireContext(), getString(R.string.failed_to_load_image))
                        e?.printStackTrace()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .transform(CenterCrop())
                .into(binding.photoEditorView.source)
        }

        photoEditor = PhotoEditor.Builder(requireContext(), binding.photoEditorView)
            .setPinchTextScalable(true)
            //.setDefaultTextTypeface() TODO: Implementation, Only fonts.
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build()

        binding.recyclerViewEditingTool.apply {
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = GridLayoutManager.HORIZONTAL
            }
            adapter = editingToolRecyclerViewAdapter
        }

        brushPropertiesBottomSheetDialogFragment = BrushPropertiesBottomSheetDialogFragment()
        brushPropertiesBottomSheetDialogFragment.setPropertiesChangeListener(this)

        binding.recyclerViewFilter.apply {
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = GridLayoutManager.HORIZONTAL
            }
            adapter = filterViewAdapter
        }

        emojiBottomSheetDialogFragment = EmojiBottomSheetDialogFragment()
        emojiBottomSheetDialogFragment.setEmojiListener(this)

        // stickerBottomSheetDialogFragment = StickerBottomSheetDialogFragment()
        // stickerBottomSheetDialogFragment.setStickerListener(this)

        binding.imageUndo.setOnClickListener {
            photoEditor.undo()
        }

        binding.imageRedo.setOnClickListener {
            photoEditor.redo()
        }

        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        binding.imageClose.setOnClickListener {
            backPressed()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        lockActivityOrientation(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onToolSelected(toolType: ToolType?) {
        when(toolType) {
            ToolType.BRUSH -> {
                photoEditor.setBrushDrawingMode(true)
                binding.textCurrentTool.text = getString(R.string.editing_tool_brush)
                brushPropertiesBottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    brushPropertiesBottomSheetDialogFragment.tag
                )
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment =
                    TextEditorDialogFragment.show(requireActivity() as MainActivity)
                textEditorDialogFragment.setOnTextEditorListener(object : TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        photoEditor.addText(inputText, styleBuilder)
                        binding.textCurrentTool.setText(R.string.editing_tool_text)
                    }
                })
            }
            ToolType.ERASER -> {
                photoEditor.brushEraser()
                binding.textCurrentTool.setText(R.string.editing_tool_eraser)
            }
            ToolType.FILTER -> {
                binding.textCurrentTool.setText(R.string.editing_tool_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> {
                emojiBottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    emojiBottomSheetDialogFragment.tag
                )
                binding.textCurrentTool.setText(R.string.editing_tool_emoji)
            }
            /*
            ToolType.STICKER -> {
                stickerBottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    stickerBottomSheetDialogFragment.tag
                )
                binding.textCurrentTool.setText(R.string.editing_tool_sticker)
            }
             */
            ToolType.CROP -> {
                navigateToSimpleCropViewFragment()
                binding.textCurrentTool.setText(R.string.editing_tool_crop)
            }
        }
    }

    private fun showFilter(isVisible: Boolean) {
        isFilterVisible = isVisible
        constraintSet.clone(binding.layoutFragmentPhotoEditor)
        if (isVisible) {
            constraintSet.clear(binding.recyclerViewFilter.id, ConstraintSet.START)
            constraintSet.connect(
                binding.recyclerViewFilter.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            constraintSet.connect(
                binding.recyclerViewFilter.id, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            constraintSet.connect(
                binding.recyclerViewFilter.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            constraintSet.clear(binding.recyclerViewFilter.id, ConstraintSet.END)
        }

        val changeBounds = ChangeBounds()
        changeBounds.duration = 400
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0F)
        TransitionManager.beginDelayedTransition(binding.layoutFragmentPhotoEditor, changeBounds)
        constraintSet.applyTo(binding.layoutFragmentPhotoEditor)
    }

    override fun onColorChanged(colorCode: Int) {
        photoEditor.brushColor = colorCode
        binding.textCurrentTool.text = getString(R.string.editing_tool_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        photoEditor.setOpacity(opacity)
        binding.textCurrentTool.text = getString(R.string.editing_tool_brush)

    }

    override fun onBrushSizeChanged(brushSize: Float) {
        photoEditor.brushSize = brushSize
        binding.textCurrentTool.text = getString(R.string.editing_tool_brush)
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        photoEditor.setFilterEffect(photoFilter)
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        photoEditor.addEmoji(emojiUnicode)
        binding.textCurrentTool.setText(R.string.editing_tool_emoji)
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        photoEditor.addImage(bitmap)
        binding.textCurrentTool.setText(R.string.editing_tool_sticker)
    }

    private fun navigateToSimpleCropViewFragment() {
        navigationDestination = NavigationDestination.TO_SIMPLE_CROP_VIEW_FRAGMENT

        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
            return
        }

        val filePath = PhotoHelper.createTempImageFile(
            requireContext(),
            PHOTO_EDITOR_IMAGE_FILE_NAME
        ).absolutePath
        editedImageUri = File(filePath).toUri()

        photoEditor.saveAsFile(filePath, object : OnSaveListener {
            override fun onSuccess(imagePath: String) {
                findNavController()
                    .navigate(
                        PhotoEditorFragmentDirections
                            .actionPhotoEditorFragmentToSimpleCropViewFragment(editedImageUri)
                    )
            }

            override fun onFailure(exception: Exception) {
                Timber.e(exception, "Failed to save edited image file")
            }
        })
    }

    fun backPressed() {
        if (isFilterVisible) {
            showFilter(false)
            binding.textCurrentTool.setText(R.string.edit_photo)
        } else if (!photoEditor.isCacheEmpty) {
            // TODO: Implementation - showSaveDialog()
            // 변경사항 있는 경우인듯. 물어보고 끄기.
            // findNavController().popBackStack()
            showEndDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                if (hasPermissions(
                        requireContext(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    )) {
                    when (navigationDestination) {
                        NavigationDestination.TO_DIARY_WRITING_FRAGMENT -> popBackStackWithSaveImage()
                        NavigationDestination.TO_SIMPLE_CROP_VIEW_FRAGMENT -> navigateToSimpleCropViewFragment()
                        else -> return
                    }
                }
            } else {
                showToast(requireContext(), "파일 권한을 승인하셔야 해당 기능을 사용할 수 있습니다.")
            }
        }
    }

    private fun showEndDialog() {
        okCancelDialogFragment.show(requireActivity().supportFragmentManager, tag)
    }

    private fun popBackStackWithSaveImage() {
        navigationDestination = NavigationDestination.TO_DIARY_WRITING_FRAGMENT

        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
            return
        }

        originImageUri?.let { uri ->
            var path = uri.path ?: return
            deleteOriginImageFile(path)
            path = path.replace(".jpg", "_0.jpg")

            val saveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(true)
                .build()

            photoEditor.saveAsFile(path, saveSettings, object : OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        KEY_EDITED_IMAGE_URI,
                        File(path).toUri()
                    )
                    findNavController().popBackStack()
                }

                override fun onFailure(exception: Exception) {
                    showToast(requireContext(), getString(R.string.failed_to_save_image))
                    Timber.e(exception, "Failed to save edited image file")
                    findNavController().popBackStack()
                }
            })
        } ?: run {
            showToast(requireContext(), getString(R.string.failed_to_save_image))
            findNavController().popBackStack()
        }
    }

    private fun deleteOriginImageFile(path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(path)
            if (file.exists()) {
                if (file.delete())
                    Timber.d("File deleted: ${file.name}")
                else
                    Timber.e("Failed to delete file: ${file.name}")
            }
        }
    }

    enum class NavigationDestination {
        TO_SIMPLE_CROP_VIEW_FRAGMENT,
        TO_DIARY_WRITING_FRAGMENT
    }

    companion object {
        const val PHOTO_EDITOR_IMAGE_FILE_NAME = "photo_editor_image_"
        const val KEY_EDITED_IMAGE_URI = "key_edited_image_uri"
    }
}