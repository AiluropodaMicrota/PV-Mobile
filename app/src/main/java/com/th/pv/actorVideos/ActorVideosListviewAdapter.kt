package com.th.pv.actorVideos

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.format.DateUtils
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.flexbox.FlexboxLayout
import com.th.pv.*
import com.th.pv.data.*


class ActorVideosListviewAdapter(
        private var pvData: PVData,
        var filter : VideoFilter,
        var sorter : VideoSort,
        context : Context,
        resource : Int
) : ArrayAdapter<String>(context, resource) {

    private var layoutInflater: LayoutInflater? = null
    var videosList : MutableList<String> = mutableListOf()

    override fun getCount(): Int {
        return videosList.size
    }

    fun update() {
        videosList = pvData.getVideos(filter, sorter)
    }

    @SuppressLint("ResourceType")
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.actor_videos_grid_item, null)
        }

        val video = pvData.videos[videosList[position]]!!

        val imageView = convertView!!.findViewById<ImageView>(R.id.gridActorVideos_imageView)
        val textView = convertView.findViewById<TextView>(R.id.gridActorVideos_textView)

        glideLoadInto(context, imageView, pvData, pvData.images[video.thumbnail])

        textView.text = video.name
        if (video.loaded)
            textView.setTypeface(null, Typeface.BOLD)
        else
            textView.setTypeface(null, Typeface.NORMAL)

        video.meta.duration?.let {
            convertView.findViewById<TextView>(R.id.gridActorVideos_duration).text =
                DateUtils.formatElapsedTime(it.toLong())
        }

        val ratingView = convertView.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(R.id.rating)
        ratingView.rating = video.rating.toFloat() / 2

        var labelsLay = convertView.findViewById<FlexboxLayout>(R.id.labels)
        labelsLay.removeAllViews()

        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(4, 4, 4, 4)

        for (i in video.labels) {
            val label = TextView(ContextThemeWrapper(context, R.style.Theme_PV), null, 0)
            label.setPadding(16, 8, 16, 8)
            label.layoutParams = params
            label.text = pvData.labels[i]!!.name
            label.setBackgroundResource(R.layout.rounded_border)
            labelsLay.addView(label)
        }

        return convertView
    }
}