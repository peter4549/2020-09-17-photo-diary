package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R

class ColorPickerAdapter internal constructor(context: Context, colorPickerColors: List<Int>) :
    RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    private var context: Context
    private var inflater: LayoutInflater
    private val colorPickerColors: List<Int>
    private var onColorPickerClickListener: OnColorPickerClickListener? = null

    internal constructor(context: Context) : this(context, getDefaultColors(context)) {
        this.context = context
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(R.layout.item_color_picker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.colorPickerView.setBackgroundColor(colorPickerColors[position])
    }

    override fun getItemCount(): Int {
        return colorPickerColors.size
    }

    private fun buildColorPickerView(view: View, colorCode: Int) {
        view.visibility = View.VISIBLE
        val biggerCircle = ShapeDrawable(OvalShape())
        biggerCircle.intrinsicHeight = 20
        biggerCircle.intrinsicWidth = 20
        biggerCircle.bounds = Rect(0, 0, 20, 20)
        biggerCircle.paint.color = colorCode

        val smallerCircle = ShapeDrawable(OvalShape())
        smallerCircle.intrinsicHeight = 5
        smallerCircle.intrinsicWidth = 5
        smallerCircle.bounds = Rect(0, 0, 5, 5)
        smallerCircle.paint.color = Color.WHITE
        smallerCircle.setPadding(10, 10, 10, 10)

        val drawables = arrayOf<Drawable>(smallerCircle, biggerCircle)
        val layerDrawable = LayerDrawable(drawables)
        view.background = layerDrawable
    }

    fun setOnColorPickerClickListener(onColorPickerClickListener: OnColorPickerClickListener?) {
        this.onColorPickerClickListener = onColorPickerClickListener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var colorPickerView: View = itemView.findViewById(R.id.color_picker_view)

        init {
            itemView.setOnClickListener {
                if (onColorPickerClickListener != null)
                    onColorPickerClickListener?.onColorPickerClickListener(
                    colorPickerColors[absoluteAdapterPosition]
                )
            }
        }
    }

    interface OnColorPickerClickListener {
        fun onColorPickerClickListener(colorCode: Int)
    }

    companion object {
        fun getDefaultColors(context: Context): List<Int> {
            val colorPickerColors: ArrayList<Int> = ArrayList()
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorRed500))
            colorPickerColors.add(ContextCompat.getColor(context, android.R.color.white))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorPink500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorPurple500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorDeepPurple500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorIndigo500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorBlue500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorLightBlue500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorCyan500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorTeal500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorGreen500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorLightGreen500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorLime500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorYellow500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorAmber500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorOrange500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorDeepOrange500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorBrown500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorGrey500))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.colorBlueGrey500))
            colorPickerColors.add(ContextCompat.getColor(context, android.R.color.black))
            return colorPickerColors
        }
    }

    init {
        this.context = context
        inflater = LayoutInflater.from(context)
        this.colorPickerColors = colorPickerColors
    }
}