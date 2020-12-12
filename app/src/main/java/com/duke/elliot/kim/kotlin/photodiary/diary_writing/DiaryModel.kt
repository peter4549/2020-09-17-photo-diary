package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel
import kotlinx.android.parcel.Parcelize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "diary")
@Parcelize
data class DiaryModel(@PrimaryKey(autoGenerate = true)
                      val id: Long = 0,
                      var time: Long,
                      var title: String,
                      var content: String,
                      var mediaArray: Array<MediaModel> = arrayOf(),
                      var textOptions: TextOptionsModel,
                      var liked: Boolean,
                      var weatherIconIndex: Int,
                      var hashTags: Array<String>,
                      var backedUp: Boolean = false,
                      var folder: FolderModel) : Parcelable {


    fun getLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this.time)
            .atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun getAllMediaAbsolutePaths(): List<String> {
        return mediaArray.map { it.uriString }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiaryModel

        if (id != other.id) return false
        if (time != other.time) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (!mediaArray.contentEquals(other.mediaArray)) return false
        if (textOptions != other.textOptions) return false
        if (liked != other.liked) return false
        if (weatherIconIndex != other.weatherIconIndex) return false
        if (!hashTags.contentEquals(other.hashTags)) return false
        if (backedUp != other.backedUp) return false
        if (folder != other.folder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + mediaArray.contentHashCode()
        result = 31 * result + textOptions.hashCode()
        result = 31 * result + liked.hashCode()
        result = 31 * result + weatherIconIndex
        result = 31 * result + hashTags.contentHashCode()
        result = 31 * result + backedUp.hashCode()
        result = 31 * result + folder.hashCode()
        return result
    }


}