package com.th.pv

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.RangeSlider
import com.th.pv.actorVideos.ActorVideosFragment
import com.th.pv.data.VideoFilter
import com.th.pv.data.VideoSort
import kotlin.math.max
import kotlin.math.min

class VideoSortFragment (
        val listener : ActorVideosFragment
) : DialogFragment() {
    var newSort : VideoSort = listener.sorter.copy()
    lateinit var dialog : AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_sort, null)

            val sortTypeSpinner = root.findViewById<Spinner>(R.id.sort_type_spinner)
            if (listener.sorter.type == VideoSort.Type.RATING)
                sortTypeSpinner.setSelection(0)
            else if (listener.sorter.type == VideoSort.Type.DURATION)
                sortTypeSpinner.setSelection(1)

            val sortDirSpinner = root.findViewById<Spinner>(R.id.sort_direction_spinner)
            if (listener.sorter.ascending)
                sortDirSpinner.setSelection(0)
            else
                sortDirSpinner.setSelection(1)

            builder.setView(root)
                    .setPositiveButton("Ok"
                    ) { dialog, id ->
                        if (sortTypeSpinner.selectedItemId == 0L)
                            newSort.type = VideoSort.Type.RATING
                        else if (sortTypeSpinner.selectedItemId == 1L)
                            newSort.type = VideoSort.Type.DURATION

                        newSort.ascending = (sortDirSpinner.selectedItemId == 0L)

                        listener.onSortDialogPositiveClick(this)
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