package com.th.pv.actorImages

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.th.pv.R
import com.th.pv.data.Actor
import com.th.pv.data.PVData
import com.th.pv.glideLoadInto

class ActorImagesAdapter(
    context : Context,
    private var pvData: PVData,
    private var actor : Actor
) : ArrayAdapter<String>(context, R.layout.actor_image_viewholder) {

    private var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int {
        return actor.images.size
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
            convertView = layoutInflater!!.inflate(R.layout.actor_image_viewholder, null)
        }

        val image = pvData.images[actor.images[position]]!!
        val imageView = convertView!!.findViewById<ImageView>(R.id.imageview)

        glideLoadInto(context, imageView, pvData, image)

        return convertView
    }
}