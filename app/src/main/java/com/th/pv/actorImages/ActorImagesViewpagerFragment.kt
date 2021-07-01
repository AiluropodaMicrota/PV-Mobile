package com.th.pv.actorImages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.th.pv.MainActivity
import com.th.pv.R
import com.th.pv.data.Actor
import com.th.pv.data.PVData

class ActorImagesViewpagerFragment : Fragment() {
    lateinit var pvData : PVData
    lateinit var actor : Actor
    private lateinit var viewPagerAdapter: ActorImagesViewpagerAdapter
    private lateinit var viewpager : ViewPager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pvData = (activity as MainActivity).pvData
        return inflater.inflate(R.layout.actor_images_viewpager_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        actor = pvData.actors[arguments?.getString("actorId")!!]!!

        viewpager = view.findViewById(R.id.viewpager)
        viewPagerAdapter = ActorImagesViewpagerAdapter(view.context, pvData, actor)
        viewpager.adapter = viewPagerAdapter
        viewpager.setCurrentItem(arguments?.getInt("position")!!, false)
    }

    override fun onDestroy() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        (activity as? AppCompatActivity)?.supportActionBar?.show()

        super.onDestroy()
    }
}