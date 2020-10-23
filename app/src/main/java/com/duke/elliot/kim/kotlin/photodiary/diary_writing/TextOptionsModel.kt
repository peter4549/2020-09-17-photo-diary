package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextOptionsModel (var textAlignment: Int,
                             var textColor: Int,
                             var textFontId: Int,
                             var textSize: Float,
                             var textStyleBold: Boolean,
                             var textStyleItalic: Boolean): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextOptionsModel

        if (textAlignment != other.textAlignment) return false
        if (textColor != other.textColor) return false
        if (textFontId != other.textFontId) return false
        if (textSize != other.textSize) return false
        if (textStyleBold != other.textStyleBold) return false
        if (textStyleItalic != other.textStyleItalic) return false

        return true
    }

    override fun hashCode(): Int {
        var result = textAlignment
        result = 31 * result + textColor
        result = 31 * result + textFontId
        result = 31 * result + textSize.hashCode()
        result = 31 * result + textStyleBold.hashCode()
        result = 31 * result + textStyleItalic.hashCode()
        return result
    }
}
