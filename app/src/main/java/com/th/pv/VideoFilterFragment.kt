package com.th.pv

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.RangeSlider
import com.th.pv.actorVideos.ActorVideosFragment
import com.th.pv.data.VideoFilter
import kotlin.math.max
import kotlin.math.min

class VideoFilterFragment(
        val listener : ActorVideosFragment
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_filter, null)

            root.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(R.id.filterMinRatingBar).rating = listener.filter.minRating.toFloat() / 2
            root.findViewById<CheckBox>(R.id.onlyFavouriteCheckbox).isChecked = listener.filter.onlyFavourite

            var maxDuration = 0.0f
            val flt = VideoFilter()
            flt.actorsOr = listener.filter.actorsOr
            val actorVideos = listener.pvData.filterVideos(flt)
            for (id in actorVideos) {
                val video = listener.pvData.videos[id]!!
                if (video.meta.duration != null)
                    maxDuration = max(maxDuration, video.meta.duration!!.toFloat())
            }
            val durationSlider = root.findViewById<RangeSlider>(R.id.durationSlider)
            durationSlider.valueTo = maxDuration + 1
            durationSlider.values = listOf(
                listener.filter.minDuration.toFloat(),
                min(durationSlider.valueTo, listener.filter.maxDuration.toFloat())
            )
            durationSlider.setLabelFormatter{
                value ->
                    val secs = value.toInt()
                    if (value == durationSlider.valueTo)
                        "Inf"
                    else
                        String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60)
            }

            builder.setView(root)
                    .setPositiveButton("Ok"
                    ) { dialog, id ->
                        listener.onDialogPositiveClick(this)
                    }
                    .setNegativeButton("Cancel"
                    ) { dialog, id ->
                        dialog.cancel()
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}