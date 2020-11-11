package com.duke.elliot.kim.kotlin.photodiary.utility

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri

internal class MediaScanner private constructor(private val context: Context) {
    private var path: String? = null
    private var mediaScannerConnection: MediaScannerConnection? = null
    private var mediaScannerConnectionClient: MediaScannerConnectionClient? = null

    fun scanMedia(path: String) {
        if (mediaScannerConnection == null) {
            mediaScannerConnectionClient = object : MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {
                    mediaScannerConnection!!.scanFile(path, null)
                }

                override fun onScanCompleted(path: String?, uri: Uri?) {  }
            }
            mediaScannerConnection = MediaScannerConnection(context, mediaScannerConnectionClient)
        }
        this.path = path
        mediaScannerConnection!!.connect()
    }

    companion object {
        fun newInstance(context: Context): MediaScanner {
            return MediaScanner(context)
        }
    }
}

