package com.th.pv

import android.app.Dialog
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.th.pv.data.Actor
import com.th.pv.data.ActorVideo
import com.th.pv.data.PVData
import kotlin.math.round

class VideoEditFragment(var pvData: PVData, var video : ActorVideo) : DialogFragment() {
    lateinit var dialog : AlertDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_edit, null)

            val ratingBar = root.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(R.id.ratingBar)
            ratingBar.rating = video.rating.toFloat() / 2

            val videoActorSelectorAdapter = ActorSelectorAdapter(requireActivity(), pvData)
            var videoActorSelector = root.findViewById<ListView>(R.id.videoActorsSelector)
            videoActorSelector.adapter = videoActorSelectorAdapter
            videoActorSelector.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            for (i in 0 until videoActorSelector.count){
                val ac = videoActorSelector.getItemAtPosition(i) as Actor
                videoActorSelector.setItemChecked(i, ac.id in video.actors)
            }

            builder.setView(root)
                .setPositiveButton("Ok"
                ) { dialog, id ->
                    video.rating = round(ratingBar.rating * 2).toInt()

                    video.actors.clear()
                    for (i in 0 until videoActorSelector.count){
                        val ac = videoActorSelector.getItemAtPosition(i) as Actor
                        if (videoActorSelector.isItemChecked(i))
                            video.actors.add(ac.id)
                    }

                    postVideoData(activity as MainActivity, video) {}
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