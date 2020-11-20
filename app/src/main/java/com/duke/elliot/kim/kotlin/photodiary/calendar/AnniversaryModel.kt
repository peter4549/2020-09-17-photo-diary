package com.duke.elliot.kim.kotlin.photodiary.calendar

import androidx.annotation.ColorInt
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "anniversary")
data class AnniversaryModel(@PrimaryKey(autoGenerate = true)
                            val id: Long = 0,
                            var year: Int,
                            var month: Int,
                            var day: Int,
                            var title: String,
                            @ColorInt
                            var color: Int,
                            var annual: Boolean) {

    fun getLocalDate(): LocalDate = LocalDate.of(year, month, day)
}