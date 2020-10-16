package com.duke.elliot.kim.kotlin.photodiary.diary

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary")
data class DiaryModel(@PrimaryKey(autoGenerate = true)
                      val id: Long = 0,
                      var date: String,
                      var time: String,
                      var title: String,
                      var content: String,
                      val uriList: MutableList<Uri>)