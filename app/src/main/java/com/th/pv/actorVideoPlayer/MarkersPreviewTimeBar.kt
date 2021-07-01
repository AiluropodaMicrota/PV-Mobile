package com.th.pv.actorVideoPlayer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import com.github.rubensousa.previewseekbar.exoplayer.PreviewTimeBar
import com.google.android.exoplayer2.SimpleExoPlayer
import com.th.pv.data.ActorVideo
import com.th.pv.data.PVData

class MarkersPreviewTimeBar(context : Context, attrs : AttributeSet) : PreviewTimeBar(context, attrs) {
    private val scrubberRadius = 12 //Dp, equals app:scrubber_dragged_size / 2
    private val markerWidth = 2 //dp
    private val markerHeight = 0.5 //% of time bar

    private var markerPaint = Paint()
    var pvData : PVData? = null
    var video : ActorVideo? = null

    init {
        markerPaint.setARGB(255, 255, 255, 255)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density

        if (video != null && pvData != null) {
            for (i in video!!.markers) {
                val marker = pvData!!.markers[i]!!
                val timeBarWidth = width - 2 * scrubberRadius * density
                val x = scrubberRadius * density + marker.time.toDouble() / video!!.meta.duration!! * timeBarWidth

                canvas.drawRect(
                    x.toFloat() - markerWidth * density,
                    (height * (1 - markerHeight) / 2).toFloat(),
                    x.toFloat() + markerWidth * density,
                    (height - height * (1 - markerHeight) / 2).toFloat(),
                    markerPaint
                )
            }
        }
    }
}