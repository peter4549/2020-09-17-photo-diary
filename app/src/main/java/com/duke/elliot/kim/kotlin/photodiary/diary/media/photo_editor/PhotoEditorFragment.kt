package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPhotoEditorBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor.TextEditorDialogFragment.TextEditor
import com.duke.elliot.kim.kotlin.photodiary.diary.media.simple_crop_view.SimpleCropViewFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.bitmapToImageFile
import com.duke.elliot.kim.kotlin.photodiary.utility.lockActivityOrientation
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter
import ja.burhanrashid52.photoeditor.TextStyleBuilder

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
    private lateinit var stickerBottomSheetDialogFragment: StickerBottomSheetDialogFragment
    private lateinit var viewModel: PhotoEditorViewModel
    private lateinit var viewModelFactory: PhotoEditorFactory
    private val constraintSet = ConstraintSet()
    private val editingToolRecyclerViewAdapter = EditingToolRecyclerViewAdapter(this)
    private val filterViewAdapter = FilterViewAdapter(this)
    private var isFilterVisible = false

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

        findNavController().currentBackStackEntry
            ?.savedStateHandle?.get<Uri>(SimpleCropViewFragment.KEY_CROPPED_BITMAP_URI)
            ?.let { imageUri = it }

        viewModelFactory = PhotoEditorFactory()
        viewModel = ViewModelProvider(this, viewModelFactory)[PhotoEditorViewModel::class.java]
        imageUri?.let { viewModel.updateImage(it) } ?: run {
            showToast(requireContext(), getString(R.string.failed_to_load_image))
        }

        viewModel.imageUri.observe(viewLifecycleOwner) {
            // setImage(binding.photoEditorView.source, it)
            Glide.with(requireContext())
                .load(it)
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                .skipMemoryCache(false)
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

        stickerBottomSheetDialogFragment = StickerBottomSheetDialogFragment()
        stickerBottomSheetDialogFragment.setStickerListener(this)

        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showToast(requireContext(), "BACK PRESSED")
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

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
            ToolType.STICKER -> {
                stickerBottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    stickerBottomSheetDialogFragment.tag
                )
                binding.textCurrentTool.setText(R.string.editing_tool_sticker)
            }
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
        binding.textCurrentTool.setText(R.string.editing_tool_sticker);
    }

    private fun navigateToSimpleCropViewFragment() {
        (binding.photoEditorView.source.drawable as BitmapDrawable).bitmap.also { bitmap ->
            val imageUri = bitmapToImageFile(requireContext(), bitmap, EDITED_BITMAP_IMAGE_FILE)?.toUri()
            findNavController()
                .navigate(
                    PhotoEditorFragmentDirections
                        .actionPhotoEditorFragmentToSimpleCropViewFragment(imageUri)
                )
        }
        /* TODO: If above code are work well, remove this comment and code below.
            bitmapToImageFile(bitmap)?.absolutePath?.let { path ->
                findNavController()
                    .navigate(PhotoEditorFragmentDirections
                        .actionPhotoEditorFragmentToSimpleCropViewFragment(bitmap))
            } ?: run {
                showToast(requireContext(), "에디터를 열지 못했습니다.")
            }
        }

         */
    }

    fun backPressed() {
        if (isFilterVisible) {
            showFilter(false)
            binding.textCurrentTool.setText(R.string.edit_photo)
        } else if (!photoEditor.isCacheEmpty) {
            // TODO: Implementation - showSaveDialog()
        } else {
            requireActivity().onBackPressed()
        }
    }

    companion object {
        const val EDITED_BITMAP_IMAGE_FILE = "PhotoEditorBitmapImageFile"
    }

    /*
    photo.saveAsFile(filePath, new PhotoEditor.OnSaveListener() {
        @Override
        public void onSuccess(@NonNull String imagePath) {
            Log.e("PhotoEditor","Image Saved Successfully");
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.e("PhotoEditor","Failed to save Image");
        }
    });

     */
}