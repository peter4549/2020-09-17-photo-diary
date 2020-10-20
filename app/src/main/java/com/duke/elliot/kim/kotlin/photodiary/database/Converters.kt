package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.room.TypeConverter
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.TextOptionsModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun arrayToJson(value: Array<MediaModel>): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToArrayList(value: String): Array<MediaModel> = Gson().fromJson(value, Array<MediaModel>::class.java)

    @TypeConverter
    fun textOptionsToJson(value: TextOptionsModel): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToTextOptions(value: String): TextOptionsModel {
        return Gson().fromJson(value, TextOptionsModel::class.java)
    }
}