package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentPhotoEditorBinding
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter


class PhotoEditorFragment: Fragment(),
    EditingToolRecyclerViewAdapter.OnItemSelected,
    BrushPropertiesFragment.Properties {

    private lateinit var binding: FragmentPhotoEditorBinding
    private lateinit var brushPropertiesFragment: BrushPropertiesFragment
    private lateinit var photoEditor: PhotoEditor
    private val editingToolRecyclerViewAdapter = EditingToolRecyclerViewAdapter(this)

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
        val bitmap = photoEditorFragmentArgs.bitmap

        binding.photoEditorView.source.setImageBitmap(requireNotNull(bitmap))
        photoEditor = PhotoEditor.Builder(requireContext(), binding.photoEditorView)
            .setPinchTextScalable(true)
            //.setDefaultTextTypeface() TODO: Implementation
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build().apply {
                // addEmoji(PhotoEditor.getEmojis(requireContext()))
                // setBrushDrawingMode(true)
                setFilterEffect(PhotoFilter.BRIGHTNESS)
            }

        binding.recyclerViewEditingTool.apply {
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = GridLayoutManager.HORIZONTAL
            }
            adapter = editingToolRecyclerViewAdapter
        }

        brushPropertiesFragment = BrushPropertiesFragment()
        brushPropertiesFragment.setPropertiesChangeListener(this)

        return binding.root
    }

    override fun onToolSelected(toolType: ToolType?) {
        when(toolType) {
            ToolType.BRUSH -> {
                photoEditor.setBrushDrawingMode(true)
                binding.textCurrentTool.text = getString(R.string.editing_tool_brush)
                brushPropertiesFragment.show(requireActivity().supportFragmentManager,
                    brushPropertiesFragment.tag)
            }
        }
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
/*
    @Override
    public void onColorChanged(int colorCode) {
        mPhotoEditor.setBrushColor(colorCode);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onOpacityChanged(int opacity) {
        mPhotoEditor.setOpacity(opacity);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onBrushSizeChanged(int brushSize) {
        mPhotoEditor.setBrushSize(brushSize);
        mTxtCurrentTool.setText(R.string.label_brush);
    }


 */

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