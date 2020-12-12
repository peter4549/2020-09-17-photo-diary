package com.duke.elliot.kim.kotlin.photodiary.folder

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "folder")
@Parcelize
data class FolderModel(@PrimaryKey(autoGenerate = true)
                       val id: Long,
                       var name: String,
                       var color: Int) : Parcelable