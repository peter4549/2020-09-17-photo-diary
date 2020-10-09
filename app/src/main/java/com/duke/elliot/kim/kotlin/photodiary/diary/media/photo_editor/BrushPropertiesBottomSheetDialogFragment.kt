package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentBrushPropertiesBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BrushPropertiesBottomSheetDialogFragment : BottomSheetDialogFragment(), OnSeekBarChangeListener {

    private lateinit var binding: FragmentBrushPropertiesBottomSheetDialogBinding
    private var properties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onBrushSizeChanged(brushSize: Float)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_brush_properties_bottom_sheet_dialog,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.seekBarOpacity.setOnSeekBarChangeListener(this)
        binding.seekBarSize.setOnSeekBarChangeListener(this)
        binding.recyclerViewColor.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = GridLayoutManager.HORIZONTAL
            }
            adapter = ColorPickerAdapter(requireContext()).apply {
                setOnColorPickerClickListener(object :
                    ColorPickerAdapter.OnColorPickerClickListener {
                    override fun onColorPickerClickListener(colorCode: Int) {
                        if (properties != null) {
                            dismiss()
                            properties?.onColorChanged(colorCode)
                        }
                    }
                })
            }
        }
    }

    fun setPropertiesChangeListener(properties: Properties?) {
        this.properties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.seek_bar_opacity -> if (properties != null) {
                properties?.onOpacityChanged(i)
            }
            R.id.seek_bar_size -> if (properties != null) {
                properties?.onBrushSizeChanged(i.toFloat())
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {  }
    override fun onStopTrackingTouch(seekBar: SeekBar) {  }
}