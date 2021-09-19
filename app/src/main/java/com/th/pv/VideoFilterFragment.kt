package com.th.pv

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
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
    var newFilter : VideoFilter = listener.filter.copy()
    lateinit var dialog : AlertDialog
    lateinit var durationSlider : RangeSlider
    lateinit var durRangeText : TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_filter, null)

            val ratingBar = root.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(R.id.filterMinRatingBar)
            ratingBar.rating = listener.filter.minRating.toFloat() / 2
            ratingBar.setOnRatingChangeListener { ratingBar, rating ->
                newFilter.minRating = (rating * 2).toInt()
                updateFilter()
            }

            val favCheckBox = root.findViewById<CheckBox>(R.id.onlyFavouriteCheckbox)
            favCheckBox.isChecked = listener.filter.onlyFavourite
            favCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                newFilter.onlyFavourite = isChecked
                updateFilter()
            }

            durationSlider = root.findViewById(R.id.durationSlider)
            durationSlider.valueTo = getDurationSliderValueTo()
            durationSlider.values = listOf(
                listener.filter.minDuration.toFloat(),
                min(durationSlider.valueTo, listener.filter.maxDuration.toFloat())
            )
            durationSlider.addOnChangeListener { slider, value, fromUser ->
                newFilter.minDuration = durationSlider.values[0].toDouble()
                newFilter.maxDuration = durationSlider.values[1].toDouble()
                updateFilter()
            }

            durRangeText = root.findViewById(R.id.durRangeText)
            durRangeText.text = formatTime(durationSlider.values[0].toInt()) + " - " + formatTime(durationSlider.values[1].toInt())

            builder.setView(root)
                    .setPositiveButton("Ok (" + listener.pvData.filterVideos(newFilter).size + " videos)"
                    ) { dialog, id ->
                        listener.onFilterDialogPositiveClick(this)
                    }
                    .setNegativeButton("Cancel"
                    ) { dialog, id ->
                        dialog.cancel()
                    }
            dialog = builder.create()
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun updateFilter() {
        val maxDur = getDurationSliderValueTo()
        var minDurPos = durationSlider.values[0]
        if (minDurPos == durationSlider.valueTo)
            minDurPos = maxDur
        var maxDurPos = durationSlider.values[1]
        if (maxDurPos == durationSlider.valueTo)
            maxDurPos = maxDur

        durationSlider.values = listOf(
            min(minDurPos, maxDur),
            min(maxDurPos, maxDur)
        )
        durationSlider.valueTo = maxDur

        durRangeText.text = formatTime(durationSlider.values[0].toInt()) + " - " + formatTime(durationSlider.values[1].toInt())
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).text = "Ok (" + listener.pvData.filterVideos(newFilter).size + " videos)"
    }

    private fun formatTime(secs : Int) : String {
        if (secs >= 3600)
            return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60)
        else
            return String.format("%02d:%02d", secs / 60, secs % 60)
    }

    private fun getDurationSliderValueTo() : Float {
        val filter = newFilter.copy()
        filter.minDuration = 0.0
        filter.maxDuration = 36000.0

        return getMaxVideoDuration(filter) + 1
    }

    private fun getMaxVideoDuration(filter : VideoFilter) : Float {
        var maxDuration = 0.0f

        for (id in listener.pvData.filterVideos(filter)) {
            val video = listener.pvData.videos[id]!!
            if (video.meta.duration != null)
                maxDuration = max(maxDuration, video.meta.duration!!.toFloat())
        }

        return maxDuration
    }
}