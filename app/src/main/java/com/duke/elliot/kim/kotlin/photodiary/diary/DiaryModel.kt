package com.duke.elliot.kim.kotlin.photodiary.diary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary")
data class DiaryModel(@PrimaryKey(autoGenerate = true)
                      val id: Int = 0,
                      val date: String,
                      var title: String? = null,
                      var content: String,
                      val time: Long)