package com.duke.elliot.kim.kotlin.photodiary.folder

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "folder")
@Parcelize
data class FolderModel(@PrimaryKey(autoGenerate = true)
                       val id: Long = 0L,
                       var name: String,
                       var color: Int,
                       var diaryIds: Array<Long> = arrayOf()) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderModel

        if (id != other.id) return false
        if (name != other.name) return false
        if (color != other.color) return false
        if (!diaryIds.contentEquals(other.diaryIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + color
        result = 31 * result + diaryIds.contentHashCode()
        return result
    }
}