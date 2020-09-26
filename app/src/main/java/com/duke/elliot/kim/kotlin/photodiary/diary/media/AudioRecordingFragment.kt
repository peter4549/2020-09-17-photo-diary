package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import kotlinx.android.synthetic.main.fragment_audio_recording.view.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class AudioRecordingFragment: Fragment() {

    private lateinit var currentAudioPath: String
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio_recording, container, false)

        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        currentAudioPath = "${requireContext().externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        // TODO: Temp
        val dm = DisplayMetrics()
        println("DDDDDD" + dm)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireContext().display?.getRealMetrics(dm)

        } else
            requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        println("PPPPPPP" + dm)
        
        //view.wave_view.speechStarted()

        view.image_button_audio.setOnClickListener {
            view.wave_view.initialize(dm)
        }

        view.button.setOnClickListener {
            view.wave_view.speechStarted()
        }

        return view
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(currentAudioPath)
                prepare()
                start()
            } catch (e: IOException) {
                Timber.e(e, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun onRecord(start: Boolean) =
        if (start)
            startRecording()
        else
            stopRecording()

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(currentAudioPath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Timber.e(e, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
        recorder?.release()
        recorder = null
    }

    private fun createAudioFile(context: Context): File {
        val timestamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val audiosDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File.createTempFile(
            "audio_$timestamp",
            ".3gp",
            audiosDirectory
        ).apply {
            currentAudioPath = absolutePath
        }
    }

    internal inner class RecordButton(context: Context) : androidx.appcompat.widget.AppCompatButton(context) {

        var mStartRecording = true

        var clicker: OnClickListener = OnClickListener {
            onRecord(mStartRecording)
            text = when (mStartRecording) {
                true -> "Stop recording"
                false -> "Start recording"
            }
            mStartRecording = !mStartRecording
        }

        init {
            text = "Start recording"
            setOnClickListener(clicker)
        }
    }
}