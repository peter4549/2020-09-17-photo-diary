package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "diary")
@Parcelize
data class DiaryModel(@PrimaryKey(autoGenerate = true)
                      val id: Long = 0,
                      var time: Long,
                      var title: String,
                      var content: String,
                      var mediaArray: Array<MediaModel> = arrayOf(),
                      var textOptions: TextOptionsModel,
                      var liked: Boolean) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiaryModel

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}