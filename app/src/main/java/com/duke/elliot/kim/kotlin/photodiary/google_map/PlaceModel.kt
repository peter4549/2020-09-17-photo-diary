package com.duke.elliot.kim.kotlin.photodiary.google_map

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaceModel(val id: Long = 0,
                      var name: String,
                      var longitude: Double,
                      var latitude: Double): Parcelable