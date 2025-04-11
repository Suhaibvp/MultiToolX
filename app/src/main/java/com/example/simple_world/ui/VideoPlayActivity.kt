package com.example.simple_world.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import androidx.core.view.GestureDetectorCompat


import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.ui.PlayerView
import com.example.simple_world.R
import java.io.File

@UnstableApi
class VideoPlayActivity : AppCompatActivity() {
    private var hasRetriedWithSecondAudio = false
    private lateinit var playerView: PlayerView
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var player: ExoPlayer
    private var currentUri: Uri? = null

    //swipe gesture audio brightness
    private lateinit var gestureDetector: GestureDetectorCompat
    private var maxVolume = 0
    private var audioManager: AudioManager? = null
    private var initialTouchY = 0f
    private var screenHeight = 0
    private var isAdjustingVolume = false
    private var isAdjustingBrightness = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_video)
        trackSelector = DefaultTrackSelector(this)
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setPreferredAudioLanguage("en") // Optional: prioritize English
        )

        player = ExoPlayer.Builder(this).build()



        playerView = findViewById(R.id.playerView)
        hideSystemUI()

        val uri = intent?.data
        currentUri = uri
        uri?.let { setOrientationByVideo(it) }

        setupPlayer(uri)
        setupControlledUI()
        setupGestureControls()
    }
    private fun setupGestureControls() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        screenHeight = Resources.getSystem().displayMetrics.heightPixels

        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                initialTouchY = e.y
                val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                isAdjustingVolume = e.x > screenWidth / 2
                isAdjustingBrightness = e.x <= screenWidth / 2
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val deltaY = initialTouchY - e2.y
                val percent = deltaY / screenHeight

                if (isAdjustingVolume) {
                    adjustVolume(percent)
                } else if (isAdjustingBrightness) {
                    adjustBrightness(percent)
                }
                return true
            }
        })

        playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }
    private fun adjustVolume(percent: Float) {
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        val delta = (percent * maxVolume).toInt()
        val newVolume = (currentVolume + delta).coerceIn(0, maxVolume)
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        showOverlay("Volume: ${(newVolume * 100) / maxVolume}%")
    }
    private fun showOverlay(text: String) {
        val overlay = findViewById<TextView>(R.id.overlay_text)
        val container = findViewById<View>(R.id.overlay_container)

        overlay.text = text
        container.visibility = View.VISIBLE

        container.removeCallbacks(hideOverlayRunnable)
        container.postDelayed(hideOverlayRunnable, 1000)
    }

    private val hideOverlayRunnable = Runnable {
        findViewById<View>(R.id.overlay_container)?.visibility = View.GONE
    }

    private fun adjustBrightness(percent: Float) {
        val window = window
        val lp = window.attributes
        lp.screenBrightness = (lp.screenBrightness + percent).coerceIn(0.01f, 1f)
        window.attributes = lp
        val brightnessPercent = (lp.screenBrightness * 100).toInt()
        showOverlay("Brightness: $brightnessPercent%")
    }

    private fun setOrientationByVideo(uri: Uri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, uri)

        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
        val hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)

        Log.d("VideoMeta", "MIME: $mime, Duration: $duration, Video: $hasVideo, Audio: $hasAudio")

        retriever.release()
        retriever.release()

        requestedOrientation = if (width > height) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun setupPlayer(videoUri: Uri?) {
        if (videoUri == null) return

        val path = videoUri.path ?: return
        val file = File(path)
        val fixedUri = Uri.fromFile(file)

        // 1. Create track selector
        val trackSelector = DefaultTrackSelector(this)
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()

        playerView.player = player

        // 2. Add listener for fallback
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (!hasRetriedWithSecondAudio) {
                    Log.w("VideoDebug", "Playback failed, retrying with 2-channel audio...")
                    hasRetriedWithSecondAudio = true
                    tryWithTwoChannelAudio(videoUri)
                } else {
                    showPlaybackFailedDialog()
                }
            }
        })

        // 3. Setup source
        val dataSourceFactory = DefaultDataSource.Factory(this)
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
            .createMediaSource(MediaItem.fromUri(fixedUri))

        // 4. Play
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    private fun tryWithTwoChannelAudio(videoUri: Uri) {
        player.release() // Release the broken one

        val retryTrackSelector = DefaultTrackSelector(this).apply {
            parameters = buildUponParameters()
                .setMaxAudioChannelCount(2) // No surround, please
                .setPreferredAudioLanguage("ml") // optional, you can remove if unknown
                .build()
        }

        player = ExoPlayer.Builder(this)
            .setTrackSelector(retryTrackSelector)
            .build()

        playerView.player = player

        val dataSourceFactory = DefaultDataSource.Factory(this)
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
            .createMediaSource(MediaItem.fromUri(videoUri))

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                showPlaybackFailedDialog()
            }
        })

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }
    private fun showPlaybackFailedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Playback Failed")
            .setMessage("We couldn't play this video. Maybe it's cursed.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun setupControlledUI() {
        val btnSubtitle = findViewById<View>(R.id.btnSubtitle)
        val btnAudioTrack = findViewById<View>(R.id.btnAudioTrack)

        btnSubtitle.setOnClickListener {
            showSubtitlePicker()
        }

        btnAudioTrack.setOnClickListener {
            showAudioTrackPicker()
        }
    }

    @OptIn(UnstableApi::class)
    private fun showSubtitlePicker() {
        val mappedTrackInfo = (player.trackSelector as? DefaultTrackSelector)?.currentMappedTrackInfo ?: return

        for (i in 0 until mappedTrackInfo.rendererCount) {
            val trackType = mappedTrackInfo.getRendererType(i)
            if (trackType == C.TRACK_TYPE_TEXT) {
                val trackGroups = mappedTrackInfo.getTrackGroups(i)
                for (groupIndex in 0 until trackGroups.length) {
                    val group = trackGroups[groupIndex]
                    for (trackIndex in 0 until group.length) {
                        val format = group.getFormat(trackIndex)
                        val label = format.label ?: "Unknown"
                        val language = format.language ?: "und"
                        Log.d("SubtitlePicker", "Subtitle Track [$groupIndex][$trackIndex]: $label (Language: $language)")
                    }
                }
            }
        }
    }


    @OptIn(UnstableApi::class)
    private fun showAudioTrackPicker() {
        val mappedTrackInfo = (player.trackSelector as? DefaultTrackSelector)?.currentMappedTrackInfo ?: return

        for (i in 0 until mappedTrackInfo.rendererCount) {
            val trackType = mappedTrackInfo.getRendererType(i)
            if (trackType == C.TRACK_TYPE_AUDIO) {
                val trackGroups = mappedTrackInfo.getTrackGroups(i)
                for (groupIndex in 0 until trackGroups.length) {
                    val group = trackGroups.get(groupIndex)
                    for (trackIndex in 0 until group.length) {
                        val format = group.getFormat(trackIndex)
                        Log.d("AudioTrack", "Track: ${format.language} - ${format.label}")
                    }
                }
            }
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    enum class TrackType {
        SUBTITLE, AUDIO
    }
}
