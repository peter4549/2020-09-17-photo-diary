package com.duke.elliot.kim.kotlin.photodiary.google_map

import androidx.lifecycle.ViewModel

class GoogleMapViewModel: ViewModel() {
    var initialized = false
    var place: PlaceModel? = null
}