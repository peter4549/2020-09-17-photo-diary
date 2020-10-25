package com.duke.elliot.kim.kotlin.photodiary.view

import android.content.Context
import android.util.AttributeSet

class CustomSpinner : androidx.appcompat.widget.AppCompatSpinner {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super (
        context,
        attrs,
        defStyle
    )

    override fun setSelection(position: Int, animate: Boolean) {
        val sameItemSelected = position == selectedItemPosition
        super.setSelection(position, animate)

        if (sameItemSelected)
            onItemSelectedListener?.onItemSelected(this, selectedView, position, selectedItemId)
    }

    override fun setSelection(position: Int) {
        val sameItemSelected = position == selectedItemPosition
        super.setSelection(position)

        if (sameItemSelected)
            onItemSelectedListener?.onItemSelected(this, selectedView, position, selectedItemId)
    }
}