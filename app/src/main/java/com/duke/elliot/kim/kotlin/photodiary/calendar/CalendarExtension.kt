package com.duke.elliot.kim.kotlin.photodiary.calendar

import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

internal fun TextView.setTextColorRes(@ColorRes colorRes: Int) = setTextColor(ContextCompat.getColor(context, colorRes))
