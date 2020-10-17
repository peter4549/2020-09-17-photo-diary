package com.duke.elliot.kim.kotlin.photodiary.diary

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel

@Entity(tableName = "diary")
data class DiaryModel(@PrimaryKey(autoGenerate = true)
                      val id: Long = 0,
                      var time: Long,
                      var title: String,
                      var content: String,
                      var mediaArray: Array<MediaModel> = arrayOf(),
                      var textOptions: TextOptionsModel) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiaryModel

        if (!mediaArray.contentEquals(other.mediaArray)) return false

        return true
    }

    override fun hashCode(): Int {
        return mediaArray.contentHashCode()
    }
}