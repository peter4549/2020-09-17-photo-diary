package com.duke.elliot.kim.kotlin.photodiary.fluid_keyboard_resize

data class KeyboardVisibilityChanged(
    val visible: Boolean,
    val contentHeight: Int,
    val contentHeightBeforeResize: Int
)