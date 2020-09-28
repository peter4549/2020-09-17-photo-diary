package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import com.duke.elliot.kim.kotlin.photodiary.diary.media.audio.PriorityRunnable
import timber.log.Timber
import java.io.FileNotFoundException
import kotlin.math.max

internal class AudioRecorder {

    private val recorderStateMonitor = Object()
    @Volatile
    private var recorderState = 0
    private var recordingCallback: RecordingCallback? = null

    fun recordingCallback(recordingCallback: RecordingCallback?): AudioRecorder {
        this.recordingCallback = recordingCallback
        return this
    }

    private fun onRecordFailure() {
        recorderState = RECORDER_STATE_FAILURE
        finishRecord()
    }

    fun startRecord() {
        if (recorderState != RECORDER_STATE_IDLE) {
            return
        }
        try {
            recorderState = RECORDER_STATE_STARTING
            startRecordThread()
        } catch (e: FileNotFoundException) {
            onRecordFailure()
            e.printStackTrace()
        }
    }

    @Throws(FileNotFoundException::class)
    private fun startRecordThread() {
        Thread(object : PriorityRunnable(Process.THREAD_PRIORITY_AUDIO) {
            private fun onExit() {
                synchronized(recorderStateMonitor) {
                    recorderState = RECORDER_STATE_IDLE
                    recorderStateMonitor.notifyAll()
                }
            }

            override fun runImpl() {
                val bufferSize = max(
                    BUFFER_BYTES_ELEMENTS * BUFFER_BYTES_PER_ELEMENT,
                    AudioRecord.getMinBufferSize(
                        RECORDER_SAMPLE_RATE,
                        RECORDER_CHANNELS_IN,
                        RECORDER_AUDIO_ENCODING
                    )
                )

                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLE_RATE,
                    RECORDER_CHANNELS_IN,
                    RECORDER_AUDIO_ENCODING,
                    bufferSize
                )

                try {
                    if (recorderState == RECORDER_STATE_STARTING) {
                        recorderState = RECORDER_STATE_BUSY
                    }
                    recorder.startRecording()
                    val recordBuffer = ByteArray(bufferSize)
                    do {
                        val bytesRead = recorder.read(recordBuffer, 0, bufferSize)
                        if (bytesRead > 0) {
                            recordingCallback!!.onDataReady(recordBuffer)
                        } else {
                            Timber.e(AudioRecorder::class.java.simpleName, "error: $bytesRead")
                            onRecordFailure()
                        }
                    } while (recorderState == RECORDER_STATE_BUSY)
                } finally {
                    recorder.release()
                }
                onExit()
            }
        }).start()
    }

    fun finishRecord() {
        var recorderStateLocal = recorderState
        if (recorderStateLocal != RECORDER_STATE_IDLE) {
            synchronized(recorderStateMonitor) {
                recorderStateLocal = recorderState
                if (recorderStateLocal == RECORDER_STATE_STARTING
                    || recorderStateLocal == RECORDER_STATE_BUSY
                ) {
                    recorderState = RECORDER_STATE_STOPPING
                    recorderStateLocal = recorderState
                }
                do {
                    try {
                        if (recorderStateLocal != RECORDER_STATE_IDLE) {
                            recorderStateMonitor.wait()
                        }
                    } catch (ignore: InterruptedException) {
                        /* Nothing to do */
                    }
                    recorderStateLocal = recorderState
                } while (recorderStateLocal == RECORDER_STATE_STOPPING)
            }
        }
    }

    val isRecording: Boolean
        get() = recorderState != RECORDER_STATE_IDLE

    internal interface RecordingCallback {
        fun onDataReady(data: ByteArray?)
    }

    companion object {
        private const val RECORDER_SAMPLE_RATE = 8000
        const val RECORDER_CHANNELS: Int = android.media.AudioFormat.CHANNEL_IN_STEREO
        const val RECORDER_AUDIO_ENCODING: Int = android.media.AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_BYTES_ELEMENTS = 1024
        private const val BUFFER_BYTES_PER_ELEMENT = RECORDER_AUDIO_ENCODING
        private const val RECORDER_CHANNELS_IN: Int = android.media.AudioFormat.CHANNEL_IN_STEREO
        const val RECORDER_STATE_FAILURE = -1
        const val RECORDER_STATE_IDLE = 0
        const val RECORDER_STATE_STARTING = 1
        const val RECORDER_STATE_STOPPING = 2
        const val RECORDER_STATE_BUSY = 3
    }
}