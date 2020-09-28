package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.cleveroad.audiovisualization.AudioVisualization
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentAudioRecordingBinding
import com.duke.elliot.kim.kotlin.photodiary.setTintByColor
import com.duke.elliot.kim.kotlin.photodiary.setTintById
import kotlinx.android.synthetic.main.fragment_audio_recording.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class AudioRecordingFragment: Fragment() {

    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioRecordingDbmHandler: AudioRecordingDbmHandler
    private lateinit var audioVisualization: AudioVisualization
    private lateinit var binding: FragmentAudioRecordingBinding
    private lateinit var currentAudioPath: String
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null
    private var state = State.INITIAL
    private val audioButtonOnClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.image_button_play -> onClickImageButtonPlay()
            R.id.image_button_record -> onClickImageButtonRecord()
            R.id.image_button_stop -> {
            }
        }
    }

    private fun onClickImageButtonPlay() {
        when(state) {
            State.PAUSE_PLAYING -> {
                image_button_play.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_circled_pause_256px
                    )
                )
                resumePlaying()
                image_button_record.isEnabled = false
                image_button_record.setTintById(R.color.colorSilver)
                state = State.PLAYING
            }
            State.PAUSE_RECORDING -> {
                image_button_play.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_circled_pause_256px
                    )
                )
                onPlay(true)
                image_button_record.isEnabled = false
                image_button_record.setTintById(R.color.colorSilver)
                state = State.PLAYING
            }
            State.PLAYING -> {
                image_button_play.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_circled_play_256px
                    )
                )
                pausePlaying()
                image_button_record.isEnabled = true
                image_button_record.setTintByColor(MainActivity.themeColorDark)
                state = State.PAUSE_PLAYING
            }
        }
    }

    private fun onClickImageButtonRecord() {
        when(state) {
            State.INITIAL -> {
                image_button_record.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_circled_pause_256px
                    )
                )

                // TODO: Set animation
                image_button_play.visibility = View.VISIBLE
                image_button_play.isEnabled = false
                image_button_play.setTintById(R.color.colorSilver)
                image_button_stop.visibility = View.VISIBLE

                audioRecorder.startRecord()
                state = State.RECORDING
            }
            State.PAUSE_PLAYING, State.PAUSE_RECORDING -> {
                image_button_record.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_circled_pause_256px
                    )
                )
                image_button_play.isEnabled = false
                image_button_play.setTintById(R.color.colorSilver)
                state = State.RECORDING
            }
            State.RECORDING -> {
                image_button_record.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_record_80
                    )
                )

                image_button_play.isEnabled = true
                image_button_play.setTintByColor(MainActivity.themeColorDark)
                state = pauseRecording()
            }
        }
    }

    private fun onClickImageButtonStop() {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_audio_recording,
            container,
            false
        )

        audioVisualization = binding.audioVisualizationView

        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )

        currentAudioPath = "${requireContext().externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        binding.imageButtonPlay.setOnClickListener(audioButtonOnClickListener)
        binding.imageButtonRecord.setOnClickListener(audioButtonOnClickListener)

        audioRecorder = AudioRecorder()
        audioRecordingDbmHandler = AudioRecordingDbmHandler()
        audioRecorder.recordingCallback(audioRecordingDbmHandler)
        audioVisualization.linkTo(audioRecordingDbmHandler)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
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

    private fun pausePlaying() {
        player?.pause()
    }

    private fun resumePlaying() {
        player?.start()
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

    private fun pauseRecording(): Int {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            recorder?.pause()
            State.PAUSE_RECORDING
        } else {
            onRecord(false)
            State.STOP_RECORDING
        }
    }

    private fun resumeRecording() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            recorder?.resume()
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
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
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

    internal inner class RecordButton(context: Context) : androidx.appcompat.widget.AppCompatButton(
        context
    ) {

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

    companion object {
        object State {
            const val INITIAL = 0
            const val PAUSE_PLAYING = 1
            const val PAUSE_RECORDING = 2
            const val PLAYING = 3
            const val RECORDING = 4
            const val STOP_RECORDING = 5
        }
    }
}