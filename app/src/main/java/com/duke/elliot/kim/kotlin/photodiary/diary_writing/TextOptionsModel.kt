package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextOptionsModel (var textAlignment: Int,
                             var textColor: Int,
                             var textFontId: Int,
                             var textSize: Float,
                             var textStyleBold: Boolean,
                             var textStyleItalic: Boolean): Parcelable
