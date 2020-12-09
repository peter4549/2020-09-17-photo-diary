package com.duke.elliot.kim.kotlin.photodiary.utility

interface Operation<T> {
    fun performAsync(callback: (T?, Throwable?) -> Unit)
}