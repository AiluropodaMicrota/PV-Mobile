package com.th.pv

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.th.pv.data.Actor
import com.th.pv.data.PVData
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ActorInfoFragment : Fragment() {
    lateinit var pvData : PVData

    lateinit var actor : Actor
    private lateinit var viewPager : ViewPager
    private lateinit var tabLayout : TabLayout

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        pvData = (activity as MainActivity).model.pvData
        return inflater.inflate(R.layout.actor_info_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        (activity as MainActivity).onFragmentCreated(this)

        actor = pvData.actors[arguments?.getString("actorId")!!]!!
        requireActivity().title = actor.name

        view.findViewById<TextView>(R.id.averageRating).text = "Average scene rating: %.1f".format(Locale.US, actor.rating)
        view.findViewById<TextView>(R.id.score).text = "PV score: %.1f".format(Locale.US, actor.score)

        val ratingView = view.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(R.id.rating)
        ratingView.rating = (actor.rating / 2).toFloat()

        view.findViewById<Button>(R.id.scenesButton).setOnClickListener {
            val bundle = bundleOf("actorId" to actor.id)
            findNavController().navigate(R.id.action_actorInfoFragment_to_actorVideosFragment, bundle)
        }

        view.findViewById<Button>(R.id.imagesButton).setOnClickListener {
            val bundle = bundleOf("actorId" to actor.id)
            findNavController().navigate(R.id.action_actorInfoFragment_to_actorImagesFragment, bundle)
        }

        glideLoadInto(view, view.findViewById(R.id.avatar), pvData, pvData.images[actor.avatar])
    }
}