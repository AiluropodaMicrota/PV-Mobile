package com.th.pv.actorImages

import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.th.pv.MainActivity
import com.th.pv.R
import com.th.pv.data.*

class ActorImagesFragment : Fragment() {
    lateinit var pvData : PVData
    lateinit var actor : Actor
    internal lateinit var actorImagesAdapter: ActorImagesAdapter
    lateinit var listView : ListView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        pvData = (activity as MainActivity).model.pvData
        return inflater.inflate(R.layout.actor_images_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        (activity as MainActivity).onFragmentCreated(this)

        actor = pvData.actors[arguments?.getString("actorId")!!]!!
        requireActivity().title = if (actor.isAlbum()) actor.name else actor.name + " - Images"

        val flexboxLayoutManager = FlexboxLayoutManager(view.context).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }

        actorImagesAdapter = ActorImagesAdapter(view.context, pvData, actor)
        listView = view.findViewById(R.id.listview)
        listView.adapter = actorImagesAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val bundle = bundleOf("actorId" to actor.id, "position" to position)
            findNavController().navigate(R.id.action_actorImagesFragment_to_actorImagesViewpagerFragment2, bundle)
        }

        if ((activity as MainActivity).model.startupRequestFinished) {
            Log.d("PV", "Starting actor images request")
            (activity as MainActivity).queryActorImages(actor)
        }
        else
            Log.d("PV", "Actor images request can't be performed now")
    }

    fun update() {
        val re = Regex("[^0-9]")

        actor.images.sortWith(compareBy({ pvData.images[it]!!.name?.replace(re, "")?.toLongOrNull() }, { pvData.images[it]!!.name}))

        actorImagesAdapter.notifyDataSetChanged()
        listView.invalidate()
    }
}