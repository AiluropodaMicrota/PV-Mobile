package com.th.pv.actorVideos

import android.app.AlertDialog
import android.os.*
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.koushikdutta.async.future.FutureCallback
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.th.pv.*
import com.th.pv.data.*
import java.io.File
import java.lang.Exception


class ActorVideosFragment : Fragment() {
    lateinit var pvData : PVData

    var listView : ListView? = null
    var listViewAdapter: ActorVideosListviewAdapter? = null
    var actor : Actor? = null
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
        val vid = pvData.videos[actor!!.videos[itemInfo.position]]!!

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
                    .setMessage(vid.meta.toString() )
                    .setPositiveButton("OK") {dialog, which ->  dialog.cancel()}
                    .show()

                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        actor = pvData.actors[arguments?.getString("actorId")!!]!!
        actor!!.videos.sortBy { !pvData.videos[it]!!.loaded }

        requireActivity().title = actor!!.name + " - Videos"

        listView = view.findViewById(R.id.list_actorVideos)
        listViewAdapter = ActorVideosListviewAdapter(pvData, actor!!, view.context, R.layout.actor_videos_grid_item)
        listView!!.adapter = listViewAdapter

        registerForContextMenu(listView!!)

        listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val bundle = bundleOf("videoId" to actor!!.videos[position])
            findNavController().navigate(R.id.action_actorVideosFragment_to_videoPlayer, bundle)
        }

        if ((activity as MainActivity).model.startupRequestFinished)
            (activity as MainActivity).queryVideos(actor)
    }

    fun update() {
        actor?.videos?.sortBy { !pvData.videos[it]!!.loaded }
        listViewAdapter?.notifyDataSetChanged()
        listView?.invalidateViews()
    }
}