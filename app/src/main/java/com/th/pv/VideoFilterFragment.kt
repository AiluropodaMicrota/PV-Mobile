package com.th.pv

import android.app.Dialog
import android.os.Bundle
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.th.pv.actorVideos.ActorVideosFragment
import kotlin.reflect.KFunction1

class VideoFilterFragment(
        val listener : ActorVideosFragment
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_filter, null)

            root.findViewById<RatingBar>(R.id.filterMinRatingBar).rating = listener.filter.minRating.toFloat() / 2

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