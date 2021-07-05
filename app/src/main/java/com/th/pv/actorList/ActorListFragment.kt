package com.th.pv.actorList

import ActorListGridviewAdapter
import android.content.Context
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.GridView
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.th.pv.*
import com.th.pv.data.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ActorListFragment : Fragment() {
    lateinit var pvData : PVData
    lateinit var gridView : GridView
    lateinit var gridViewAdapter : ActorListGridviewAdapter
    private var showAlbums : Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        pvData = (activity as MainActivity).pvData
        (activity as MainActivity).supportActionBar!!.setHomeButtonEnabled(true)
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as MainActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_drawer)
        return inflater.inflate(R.layout.actor_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAlbums = arguments?.getBoolean("showAlbums")!!

        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        requireActivity().title = if (showAlbums) "Albums" else "Actors"

        //Hide keyboard
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        gridView = view.findViewById(R.id.grid)

        gridViewAdapter = ActorListGridviewAdapter(pvData, showAlbums, view.context)
        gridView.adapter = gridViewAdapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val actor = gridViewAdapter.getItem(position)!!
            val bundle = bundleOf("actorId" to actor.id)

            if (actor.isAlbum())
                findNavController().navigate(R.id.action_actorsListFragment_to_actorImagesFragment, bundle)
            else
                findNavController().navigate(R.id.action_actorsListFragment_to_actorInfoFragment, bundle)
        }
    }

    fun update() {
        gridViewAdapter.notifyDataSetChanged()
        gridView.invalidateViews()
        requireActivity().title = if (showAlbums) "Albums" else "Actors"
    }
}