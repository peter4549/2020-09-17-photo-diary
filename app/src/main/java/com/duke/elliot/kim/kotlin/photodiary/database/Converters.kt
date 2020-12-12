package com.duke.elliot.kim.kotlin.photodiary.database

import androidx.room.TypeConverter
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.TextOptionsModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun arrayToJson(value: Array<MediaModel>): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToArray(value: String): Array<MediaModel> = Gson().fromJson(value, Array<MediaModel>::class.java)

    @TypeConverter
    fun textOptionsToJson(value: TextOptionsModel): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToTextOptions(value: String): TextOptionsModel {
        return Gson().fromJson(value, TextOptionsModel::class.java)
    }

    @TypeConverter
    fun folderToJson(value: FolderModel): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToFolder(value: String): FolderModel {
        return Gson().fromJson(value, FolderModel::class.java)
    }

    @TypeConverter
    fun stringArrayToJson(value: Array<String>): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToStringArray(value: String): Array<String> = Gson().fromJson(value, Array<String>::class.java)
}