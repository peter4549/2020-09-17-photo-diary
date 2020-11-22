package com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.ActivityVideoPlayerBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingFragment
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util

class ExoPlayerActivity: AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
    }

    private fun initializePlayer(uriString: String) {
        val uri = Uri.parse(uriString)

        player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = player

        player?.setMediaItem(MediaItem.fromUri(uri))

        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            val uriString = intent?.extras?.getString(DiaryWritingFragment.EXTRA_MEDIA_URI)
            if (uriString != null) {
                initializePlayer(uriString)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT < 24) {
            val uriString = intent?.extras?.getString(DiaryWritingFragment.EXTRA_MEDIA_URI)
            if (uriString != null) {
                initializePlayer(uriString)
            }
        }
    }

    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            binding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player?.playWhenReady ?: false
            playbackPosition = player?.currentPosition ?: 0L
            currentWindow = player?.currentWindowIndex ?: 0
            player?.release()
            player = null
        }
    }
}

