package com.th.pv.actorVideos

import android.app.AlertDialog
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.RangeSlider
import com.koushikdutta.async.future.FutureCallback
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.th.pv.*
import com.th.pv.data.*
import java.io.File
import java.lang.Exception
import java.text.FieldPosition


class ActorVideosFragment : Fragment() {
    lateinit var pvData : PVData

    var listView : ListView? = null
    var listViewAdapter: ActorVideosListviewAdapter? = null
    var progressBar : CircularProgressBar? = null
    var progressText : TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pvData = (activity as MainActivity).model.pvData
        return inflater.inflate(R.layout.actor_videos_fragment, container, false)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.actor_videos_context_menu, menu)
    }

    fun updateDownloadingProgress(downloaded : Long, total : Long) {
        if (progressBar == null || progressText == null)
            return

        val downloadedMb = downloaded / 1024 / 1024
        val totalMb = total / 1024 / 1024

        progressBar!!.apply {
            progress = downloaded.toFloat()
            progressMax = total.toFloat()
        }

        progressText!!.text = downloadedMb.toInt().toString() + " MB / " + totalMb.toInt().toString() + " MB"
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val itemInfo : AdapterView.AdapterContextMenuInfo = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val vid = pvData.videos[listViewAdapter!!.videosList[itemInfo.position]]!!

        when(item.itemId) {
            R.id.action_video_download -> {
                val progressDialog = ProgressDialog.progressDialog(requireView().context)
                progressDialog.show()

                progressBar = progressDialog.findViewById(R.id.progressBar)
                progressText = progressDialog.findViewById(R.id.loadingProgressText)

                downloadVideo(
                    activity as MainActivity,
                    this,
                    pvData,
                    vid,
                    FutureCallback { e : Exception?, result : File? ->
                        update()
                        pvData.saveData()
                        progressDialog.hide()
                    }
                    )

                return true
            }
            R.id.action_video_info -> {
                AlertDialog.Builder(context)
                    .setTitle(vid.name)
                    .setMessage(vid.toString() )
                    .setPositiveButton("OK") {dialog, which ->  dialog.cancel()}
                    .show()

                return true
            }
            R.id.action_video_edit -> {
                val frag = VideoEditFragment(vid)
                frag.show(requireActivity().supportFragmentManager, "video_edit")

                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        (activity as MainActivity).onFragmentCreated(this)

        if (arguments?.containsKey("actorId")!!) {
            val actor = pvData.actors[arguments?.getString("actorId")!!]!!
            requireActivity().title = actor.name + " - Scenes"
            pvData.filter.actorsOr.add(actor.id)

            if ((activity as MainActivity).model.startupRequestFinished)
                (activity as MainActivity).queryVideos(actor)
        }
        else {
            requireActivity().title = "Scenes"
        }

        listView = view.findViewById(R.id.list_actorVideos)
        listViewAdapter = ActorVideosListviewAdapter(pvData, view.context, R.layout.actor_videos_grid_item)
        listViewAdapter!!.update()
        listView!!.adapter = listViewAdapter

        registerForContextMenu(listView!!)

        listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val bundle = bundleOf("videoId" to listViewAdapter!!.videosList[position])
            findNavController().navigate(R.id.action_actorVideosFragment_to_videoPlayer, bundle)
        }
    }

    fun onFilterDialogPositiveClick(dialog : VideoFilterFragment) {
        pvData.filter = dialog.newFilter.copy()
        //listViewAdapter!!.filter = pvDafilter
        update()
    }

    fun onSortDialogPositiveClick(dialog : VideoSortFragment) {
        pvData.sorter = dialog.newSort.copy()
        //listViewAdapter!!.sorter = sorter
        update()
    }

    fun update() {
        listViewAdapter?.update()
        listViewAdapter?.notifyDataSetChanged()
        listView?.invalidateViews()
    }
}