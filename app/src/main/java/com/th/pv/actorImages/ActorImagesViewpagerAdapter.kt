package com.th.pv.actorImages

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.th.pv.R
import com.th.pv.data.Actor
import com.th.pv.data.PVData
import com.th.pv.glideLoadInto
import java.util.*


internal class ActorImagesViewpagerAdapter (
    var context : Context,
    var pvData: PVData,
    var actor : Actor
) :  PagerAdapter() {
    var mLayoutInflater: LayoutInflater

    override fun getCount(): Int {
        return actor.images.size
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView: View = mLayoutInflater.inflate(R.layout.actor_images_viewpage_image_item, container, false)

        val textView = itemView.findViewById<View>(R.id.imageName) as TextView
        val name = pvData.images[actor.images[position]]!!.name
        if (name != null)
            textView.text = name
        else
            textView.text = actor.name

        itemView.findViewById<TextView>(R.id.imageNum).text = (position + 1).toString() + "/" + getCount()

        val imageView = itemView.findViewById<View>(R.id.imageViewMain) as ImageView
        glideLoadInto(context, imageView, pvData, pvData.images[actor.images[position]])

        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as ConstraintLayout)
    }

    init {
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}
