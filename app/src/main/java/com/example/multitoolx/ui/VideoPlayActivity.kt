package com.example.multitoolx.ui

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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.ui.PlayerView
import com.example.multitoolx.R
import java.io.File

/**
 * VideoPlayActivity.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib vp
 * Description:
 * - Plays a video file using ExoPlayer.
 * - Supports volume and brightness adjustments via swipe gestures.
 * - Handles fallback when playback fails.
 */

// This activity handles video playback with gesture controls for volume and brightness adjustments.
@UnstableApi
class VideoPlayActivity : AppCompatActivity() {

    // Flag to check if we have retried playback with a simpler (2-channel) audio
    private var hasRetriedWithSecondAudio = false

    // Views and player components
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var trackSelector: DefaultTrackSelector
    private var currentUri: Uri? = null

    // Gesture controls variables
    private lateinit var gestureDetector: GestureDetectorCompat
    private var maxVolume = 0
    private var audioManager: AudioManager? = null
    private var initialTouchY = 0f
    private var screenHeight = 0
    private var isAdjustingVolume = false
    private var isAdjustingBrightness = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the window fullscreen and hide title
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_video)

        // Initialize the track selector with preferred language
        trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setPreferredAudioLanguage("en"))
        }

        // Create ExoPlayer instance
        player = ExoPlayer.Builder(this).build()

        // Find player view from layout
        playerView = findViewById(R.id.playerView)

        // Hide system UI (navigation bar, status bar)
        hideSystemUI()

        // Get the URI passed into this activity (e.g., from an Intent)
        val uri = intent?.data
        currentUri = uri

        // Set orientation based on video (portrait or landscape)
        uri?.let { setOrientationByVideo(it) }

        // Set up player with the video
        setupPlayer(uri)

        // Setup buttons for subtitles and audio track selection
        setupControlledUI()

        // Setup gesture detection for brightness/volume
        setupGestureControls()
    }

    // Sets up gesture detection for volume and brightness adjustments
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

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
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

    // Adjusts the device's volume based on swipe gesture
    private fun adjustVolume(percent: Float) {
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        val delta = (percent * maxVolume).toInt()
        val newVolume = (currentVolume + delta).coerceIn(0, maxVolume)
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        showOverlay("Volume: ${(newVolume * 100) / maxVolume}%")
    }

    // Adjusts the screen brightness based on swipe gesture
    private fun adjustBrightness(percent: Float) {
        val lp = window.attributes
        lp.screenBrightness = (lp.screenBrightness + percent).coerceIn(0.01f, 1f)
        window.attributes = lp
        val brightnessPercent = (lp.screenBrightness * 100).toInt()
        showOverlay("Brightness: $brightnessPercent%")
    }

    // Displays a temporary overlay text (like "Volume 50%")
    private fun showOverlay(text: String) {
        val overlay = findViewById<TextView>(R.id.overlay_text)
        val container = findViewById<View>(R.id.overlay_container)

        overlay.text = text
        container.visibility = View.VISIBLE

        container.removeCallbacks(hideOverlayRunnable)
        container.postDelayed(hideOverlayRunnable, 1000)
    }

    // Runnable to hide overlay after delay
    private val hideOverlayRunnable = Runnable {
        findViewById<View>(R.id.overlay_container)?.visibility = View.GONE
    }

    // Automatically set orientation (portrait/landscape) based on video dimensions
    private fun setOrientationByVideo(uri: Uri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, uri)

        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0

        retriever.release()

        requestedOrientation = if (width > height) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Configures the player with the provided video URI
    private fun setupPlayer(videoUri: Uri?) {
        if (videoUri == null) return

        val file = File(videoUri.path ?: return)
        val fixedUri = Uri.fromFile(file)

        playerView.player = player

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                // Retry with simpler audio format if playback fails
                if (!hasRetriedWithSecondAudio) {
                    hasRetriedWithSecondAudio = true
                    tryWithTwoChannelAudio(videoUri)
                } else {
                    showPlaybackFailedDialog()
                }
            }
        })

        val dataSourceFactory = DefaultDataSource.Factory(this)
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
            .createMediaSource(MediaItem.fromUri(fixedUri))

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    // Retry playback with maximum 2-channel audio if first attempt fails
    private fun tryWithTwoChannelAudio(videoUri: Uri) {
        player.release()

        val retryTrackSelector = DefaultTrackSelector(this).apply {
            parameters = buildUponParameters()
                .setMaxAudioChannelCount(2)
                .setPreferredAudioLanguage("ml")
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

    // Shows an alert dialog when playback completely fails
    private fun showPlaybackFailedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Playback Failed")
            .setMessage("We couldn't play this video.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // Set up click listeners for subtitle and audio track buttons
    private fun setupControlledUI() {
        findViewById<View>(R.id.btnSubtitle).setOnClickListener {
            showSubtitlePicker()
        }

        findViewById<View>(R.id.btnAudioTrack).setOnClickListener {
            showAudioTrackPicker()
        }
    }

    // Display available subtitle tracks (for debugging / future UI improvements)
    @OptIn(UnstableApi::class)
    private fun showSubtitlePicker() {
        val mappedTrackInfo = (player.trackSelector as? DefaultTrackSelector)?.currentMappedTrackInfo ?: return

        for (i in 0 until mappedTrackInfo.rendererCount) {
            if (mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_TEXT) {
                val trackGroups = mappedTrackInfo.getTrackGroups(i)
                for (groupIndex in 0 until trackGroups.length) {
                    val group = trackGroups[groupIndex]
                    for (trackIndex in 0 until group.length) {
                        val format = group.getFormat(trackIndex)
                        Log.d("SubtitlePicker", "Subtitle Track [$groupIndex][$trackIndex]: ${format.label} (${format.language})")
                    }
                }
            }
        }
    }

    // Display available audio tracks (for debugging / future UI improvements)
    @OptIn(UnstableApi::class)
    private fun showAudioTrackPicker() {
        val mappedTrackInfo = (player.trackSelector as? DefaultTrackSelector)?.currentMappedTrackInfo ?: return

        for (i in 0 until mappedTrackInfo.rendererCount) {
            if (mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_AUDIO) {
                val trackGroups = mappedTrackInfo.getTrackGroups(i)
                for (groupIndex in 0 until trackGroups.length) {
                    val group = trackGroups[groupIndex]
                    for (trackIndex in 0 until group.length) {
                        val format = group.getFormat(trackIndex)
                        Log.d("AudioTrack", "Audio Track [$groupIndex][$trackIndex]: ${format.language} (${format.label})")
                    }
                }
            }
        }
    }

    // Hides system UI for immersive video experience
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    // Release player when the activity is stopped
    override fun onStop() {
        super.onStop()
        player.release()
    }
}
