package com.th.pv.actorVideos

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import android.util.Log
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
        private var actor : Actor,
        context : Context,
        resource : Int
) : ArrayAdapter<String>(context, resource) {

    private var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int {
        return actor.videos.size
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

        val video = pvData.videos[actor.videos[position]]!!

        val imageView = convertView!!.findViewById<ImageView>(R.id.gridActorVideos_imageView)
        val textView = convertView.findViewById<TextView>(R.id.gridActorVideos_textView)

        glideLoadInto(context, imageView, pvData, pvData.images[video.thumbnail])

        textView.text = video.name
        if (video.loaded)
            textView.setTextColor(Color.BLACK)
        else
            textView.setTextColor(Color.GRAY)

        video.meta.duration?.let {
            convertView.findViewById<TextView>(R.id.gridActorVideos_duration).text =
                DateUtils.formatElapsedTime(it.toLong())
        }

        val ratingView = convertView.findViewById<FontAwesome>(R.id.rating)
        var ratingText = ""
        for (i in 0 until video.rating / 2)
            ratingText += "\uf005"
        if (video.rating % 2 == 1)
            ratingText += "\uf5c0"
        ratingView.text = ratingText

        var labelsLay = convertView.findViewById<FlexboxLayout>(R.id.labels)
        labelsLay.removeAllViews()

        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(4, 4, 4, 4)

        for (i in video.labels) {
            var label = TextView(context)
            label.setPadding(16, 8, 16, 8)
            label.layoutParams = params
            label.text = pvData.labels[i]!!.name
            label.setBackgroundResource(R.layout.rounded_border)
            labelsLay.addView(label)
        }

        return convertView
    }
}