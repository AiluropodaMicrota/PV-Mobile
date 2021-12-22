package com.th.pv

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.th.pv.data.PVData

class ActorSelectorAdapter(
        private val context: Activity,
        private val pvData: PVData
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var actorsList = mutableListOf<String>()

    init {
        for (actor in pvData.actors.keys){
            if (!pvData.actors[actor]!!.isAlbum())
                actorsList.add(actor)
        }

        actorsList.sortBy{
            pvData.actors[it]!!.name
        }
    }

    override fun getCount(): Int {
        return actorsList.size
    }

    override fun getItem(position: Int): Any {
        return pvData.actors[actorsList[position]]!!
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null)
            view = layoutInflater.inflate(R.layout.actor_selector_list_item, parent, false)

        val actorImage = view!!.findViewById(R.id.actorImage) as ImageView
        val actorName = view.findViewById(R.id.actorName) as TextView

        val actor = pvData.actors[actorsList[position]]!!
        actorName.text = actor.name
        glideLoadInto(context, actorImage, pvData, pvData.images[actor.avatar])

        return view
    }
}