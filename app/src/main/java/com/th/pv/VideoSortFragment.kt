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
import kotlin.random.Random

class VideoSortFragment (
        val listener : ActorVideosFragment
) : DialogFragment() {
    var newSort : VideoSort = listener.pvData.sorter.copy()
    lateinit var dialog : AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_sort, null)

            val sortTypeSpinner = root.findViewById<Spinner>(R.id.sort_type_spinner)
            sortTypeSpinner.setSelection(
                    when(listener.pvData.sorter.type) {
                        VideoSort.Type.RATING -> 0
                        VideoSort.Type.DURATION -> 1
                        VideoSort.Type.ALPHABETICAL -> 2
                        VideoSort.Type.RESOLUTION -> 3
                        VideoSort.Type.SIZE -> 4
                        VideoSort.Type.ADDED_ON -> 5
                        VideoSort.Type.LAST_VIEWED -> 6
                        VideoSort.Type.VIEWS -> 7
                        VideoSort.Type.RANDOM -> 8
                        else -> 0
                    }
            )

            val sortDirSpinner = root.findViewById<Spinner>(R.id.sort_direction_spinner)
            if (listener.pvData.sorter.ascending)
                sortDirSpinner.setSelection(0)
            else
                sortDirSpinner.setSelection(1)

            val loadedFirstCheckBox = root.findViewById<CheckBox>(R.id.loaded_first)
            loadedFirstCheckBox.isChecked = listener.pvData.sorter.loadedFirst

            builder.setView(root)
                    .setPositiveButton("Ok"
                    ) { dialog, id ->
                        newSort.type = when(sortTypeSpinner.selectedItemId) {
                            0L -> VideoSort.Type.RATING
                            1L -> VideoSort.Type.DURATION
                            2L -> VideoSort.Type.ALPHABETICAL
                            3L -> VideoSort.Type.RESOLUTION
                            4L -> VideoSort.Type.SIZE
                            5L -> VideoSort.Type.ADDED_ON
                            6L -> VideoSort.Type.LAST_VIEWED
                            7L -> VideoSort.Type.VIEWS
                            8L -> VideoSort.Type.RANDOM
                            else -> VideoSort.Type.RATING
                        }

                        newSort.ascending = (sortDirSpinner.selectedItemId == 0L)
                        newSort.loadedFirst = loadedFirstCheckBox.isChecked
                        newSort.randomSeed = Random.nextInt()

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