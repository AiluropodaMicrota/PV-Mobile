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
import com.th.pv.data.ActorVideo
import com.th.pv.data.VideoFilter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class VideoEditFragment(var video : ActorVideo) : DialogFragment() {
    lateinit var dialog : AlertDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_edit, null)

            val ratingBar = root.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(R.id.ratingBar)
            ratingBar.rating = video.rating.toFloat() / 2

            builder.setView(root)
                .setPositiveButton("Ok"
                ) { dialog, id ->
                    video.rating = round(ratingBar.rating * 2).toInt()
                    postVideoRating(activity as MainActivity, video) {}
                    (activity as MainActivity).update()
                }
                .setNegativeButton("Cancel"
                ) { dialog, id ->
                    dialog.cancel()
                }
            dialog = builder.create()
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}