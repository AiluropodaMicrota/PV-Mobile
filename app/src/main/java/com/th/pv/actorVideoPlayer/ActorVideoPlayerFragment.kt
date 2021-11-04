package com.th.pv.actorVideoPlayer

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.github.rubensousa.previewseekbar.PreviewBar
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.th.pv.MainActivity
import com.th.pv.R
import com.th.pv.VideoEditFragment
import com.th.pv.data.ActorVideo
import com.th.pv.data.ActorVideoMarker
import com.th.pv.data.PVData

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ActorVideoPlayerFragment : Fragment() {
    lateinit var pvData : PVData
    private val hideHandler = Handler()
    private var videoplayer : SimpleExoPlayer? = null

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        //fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        pvData = (activity as MainActivity).model.pvData

        if (pvData.videos[arguments?.getString("videoId")!!]!!.isHorizontal()) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            return inflater.inflate(R.layout.actor_video_player_fragment, container, false)
        }
        else
            return inflater.inflate(R.layout.actor_video_player_fragment_vertical, container, false)
    }

    private fun buildMediaSource(uri : Uri): MediaSource? {
        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "sample")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    /**
        Preview image width depends on previewFrameLayout width constraint
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        (activity as MainActivity).onFragmentCreated(this)

        visible = true

        val video = pvData.videos[arguments?.getString("videoId")!!]!!
        val videoview = view.findViewById<PlayerView>(R.id.videoView)!!
        var markerText = view.findViewById<TextView>(R.id.markerText)
        val frameLayout = view.findViewById<FrameLayout>(R.id.previewFrameLayout)
        val imageView = view.findViewById<ImageView>(R.id.previewImageView)
        videoplayer = SimpleExoPlayer.Builder(requireContext()).build()
        videoplayer?.playWhenReady = true
        videoview.player = videoplayer
        buildMediaSource(Uri.parse(pvData.getVideoPath(video)))?.let {
            videoplayer?.prepare(it)
        }

        view.findViewById<TextView>(R.id.videoTitle).text = video.name

        video.preview?.let {
            //imageScale = view.width.toDouble() / (it.meta.width / 100) * 0.25

            var previewTimeBar = view.findViewById<MarkersPreviewTimeBar>(R.id.exo_progress)
            val imagePreviewLoader: PreviewLoader = ImagePreviewLoader(
                imageView,
                pvData,
                it
            )
            previewTimeBar.setPreviewLoader(imagePreviewLoader)
            previewTimeBar.video = video
            previewTimeBar.pvData = pvData

            previewTimeBar.addOnScrubListener(object : PreviewBar.OnScrubListener {
                override fun onScrubStart(previewBar: PreviewBar) {
                    videoplayer?.playWhenReady = false
                    val scale = frameLayout.width / (it.meta.width / 100.0)
                    frameLayout.layoutParams.height = (markerText.height + it.meta.height * scale).toInt()
                    imageView.layoutParams.height = (it.meta.height * scale).toInt()
                    imageView.layoutParams.width = (it.meta.width / 100.0 * scale).toInt()
                    imageView.y = markerText.y + markerText.height
                }

                override fun onScrubMove(
                    previewBar: PreviewBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    var currentMarker : ActorVideoMarker? = null
                    val progressSec = progress / 1000.0

                    for (id in video.markers) {
                        val marker = pvData.markers[id]!!

                        if (marker.time < progressSec) {
                            if (currentMarker == null)
                                currentMarker = marker
                            else if (marker.time > currentMarker.time)
                                currentMarker = marker
                        }
                    }

                    if (currentMarker == null)
                        markerText.text = ""
                    else
                        markerText.text = currentMarker.name
                }

                override fun onScrubStop(previewBar: PreviewBar) {
                    videoplayer?.playWhenReady = true
                }
            })
        }

        val btnMenu = view.findViewById<ImageButton>(R.id.exo_menu)
        btnMenu.setOnClickListener{
            onMenuClicked(btnMenu, video)
        }

        view.findViewById<ImageButton>(R.id.prev_marker).setOnClickListener {
            onPrevMarkerClicked(video)
        }

        view.findViewById<ImageButton>(R.id.next_marker).setOnClickListener {
            onNextMarkerClicked(video)
        }

        if (!pvData.videos[arguments?.getString("videoId")!!]!!.isHorizontal()) {
            videoview.layoutParams.width = ViewGroup.LayoutParams.FILL_PARENT
            videoview.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    fun onMenuClicked(view : View, video : ActorVideo) {
        val popup = PopupMenu(activity, view)
        popup.inflate(R.menu.video_player_menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.editVideo -> {
                    val frag = VideoEditFragment(video)
                    frag.show(requireActivity().supportFragmentManager, "video_edit")
                }
            }

            true
        }

        popup.show()
    }

    fun onPrevMarkerClicked(video : ActorVideo) {
        if (videoplayer == null)
            return

        var prevMarker : ActorVideoMarker? = null
        var positionSec = videoplayer!!.currentPosition / 1000.0

        for (i in video.markers) {
            val marker = pvData.markers[i]!!

            if (marker.time < positionSec - 1) {
                if (prevMarker == null)
                    prevMarker = marker
                else if (marker.time > prevMarker.time)
                    prevMarker = marker
            }
        }

        if (prevMarker != null)
            videoplayer!!.seekTo((prevMarker.time * 1000.0f).toLong())
        else
            videoplayer!!.seekTo(0)
    }

    fun onNextMarkerClicked(video : ActorVideo) {
        if (videoplayer == null)
            return

        var nextMarker : ActorVideoMarker? = null
        var positionSec = videoplayer!!.currentPosition / 1000.0

        for (i in video.markers) {
            val marker = pvData.markers[i]!!

            if (marker.time > positionSec) {
                if (nextMarker == null)
                    nextMarker = marker
                else if (marker.time < nextMarker.time)
                    nextMarker = marker
            }
        }

        if (nextMarker != null)
            videoplayer!!.seekTo((nextMarker.time * 1000.0f).toLong())
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoplayer?.playWhenReady = false
        videoplayer?.release()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun hide() {
        // Hide UI first
        //fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
//        fullscreenContent?.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}