package com.th.pv

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import com.th.pv.data.Actor
import com.th.pv.data.ActorVideo
import com.th.pv.data.ActorVideoLabel
import com.th.pv.data.PVData
import kotlin.math.round


class VideoEditFragment(var pvData: PVData, var video: ActorVideo) : DialogFragment() {
    lateinit var dialog : AlertDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val root = inflater.inflate(R.layout.actor_videos_edit, null)

            val ratingBar = root.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(
                R.id.ratingBar
            )
            ratingBar.rating = video.rating.toFloat() / 2

            //Video actors list
            val layoutVideoActors = root.findViewById<net.cachapa.expandablelayout.ExpandableLayout>(R.id.videoActorsLayout)
            val iconToggleVideoActors = root.findViewById<ImageView>(R.id.iconToggleVideoActors)
            val layoutToggleVideoActors = root.findViewById<LinearLayout>(R.id.layoutToggleVideoActors)
            layoutToggleVideoActors.setOnClickListener {
                if (layoutVideoActors.isExpanded) {
                    layoutVideoActors.collapse()
                    iconToggleVideoActors.setImageResource(R.drawable.ic_expand)
                }
                else {
                    layoutVideoActors.expand()
                    iconToggleVideoActors.setImageResource(R.drawable.ic_collapse)
                }
            }

            val videoActorSelectorAdapter = ActorSelectorAdapter(requireActivity(), pvData)
            val videoActorSelector = root.findViewById<ListView>(R.id.videoActorsSelector)
            videoActorSelector.adapter = videoActorSelectorAdapter
            videoActorSelector.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            for (i in 0 until videoActorSelector.count){
                val ac = videoActorSelector.getItemAtPosition(i) as Actor
                videoActorSelector.setItemChecked(i, ac.id in video.actors)
            }

            //Video labels list
            val layoutVideoLabels = root.findViewById<net.cachapa.expandablelayout.ExpandableLayout>(R.id.videoLabelsLayout)
            val iconToggleVideoLabels = root.findViewById<ImageView>(R.id.iconToggleVideoLabels)
            val layoutToggleVideoLabels = root.findViewById<LinearLayout>(R.id.layoutToggleVideoLabels)
            layoutToggleVideoLabels.setOnClickListener {
                if (layoutVideoLabels.isExpanded) {
                    layoutVideoLabels.collapse()
                    iconToggleVideoLabels.setImageResource(R.drawable.ic_expand)
                }
                else {
                    layoutVideoLabels.expand()
                    iconToggleVideoLabels.setImageResource(R.drawable.ic_collapse)
                }
            }

            val videoLabelsSelectorAdapter = LabelSelectorAdapter(requireActivity(), pvData)
            val videoLabelsSelector = root.findViewById<ListView>(R.id.videoLabelsSelector)
            videoLabelsSelector.adapter = videoLabelsSelectorAdapter
            videoLabelsSelector.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            for (i in 0 until videoLabelsSelector.count){
                val ac = videoLabelsSelector.getItemAtPosition(i) as ActorVideoLabel
                videoLabelsSelector.setItemChecked(i, ac.id in video.labels)
            }

            builder.setView(root)
                .setPositiveButton(
                    "Ok"
                ) { dialog, id ->
                    video.rating = round(ratingBar.rating * 2).toInt()

                    video.actors.clear()
                    for (i in 0 until videoActorSelector.count){
                        val ac = videoActorSelector.getItemAtPosition(i) as Actor
                        if (videoActorSelector.isItemChecked(i))
                            video.actors.add(ac.id)
                    }

                    val oldVideoLabels = video.labels.toMutableList()
                    video.labels.clear()
                    for (i in 0 until videoLabelsSelector.count){
                        val lbl = videoLabelsSelector.getItemAtPosition(i) as ActorVideoLabel
                        if (videoLabelsSelector.isItemChecked(i))
                            video.labels.add(lbl.id)
                        else if (oldVideoLabels.contains(lbl.id))
                            removeVideoLabel(activity as MainActivity, video, lbl) {}
                    }

                    postVideoData(activity as MainActivity, video) {}
                    (activity as MainActivity).update()
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, id ->
                    dialog.cancel()
                }
            dialog = builder.create()
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}