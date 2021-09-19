 package com.th.pv

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.koushikdutta.ion.Ion
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.th.pv.actorImages.ActorImagesFragment
import com.th.pv.actorList.ActorListFragment
import com.th.pv.actorVideos.ActorVideosFragment
import com.th.pv.data.Actor
import org.json.JSONObject
import kotlin.math.min


 class MainActivity : AppCompatActivity() {
    var downloadingProgressNotificationId = 100

    var optionsMenu : Menu? = null
    var mHandler = Handler(Looper.getMainLooper())
    var notificationBuilder : Notification.Builder? = null
    var notificationManager : NotificationManager? = null
    private lateinit var imageDownloadingHandlerThread : HandlerThread
    private lateinit var imageDownloadingHandler : Handler
    private var previousImageLoadedTime : Long = 0
    private lateinit var currentFragment: Fragment

    val queryMaxTries = 10
    var topActorsQueryTriesLeft = queryMaxTries
    var videosQueryTriesLeft = queryMaxTries

    val model : PVViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        model.load(applicationContext)

        val navController = findNavController(R.id.nav_host_fragment)
        imageDownloadingHandlerThread = HandlerThread("ImageDownloadingHandlerThread")
        imageDownloadingHandlerThread.start()
        imageDownloadingHandler = Handler(imageDownloadingHandlerThread.looper)

        Ion.getDefault(applicationContext).conscryptMiddleware.enable(false);

        findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)

        val slider = findViewById<MaterialDrawerSliderView>(R.id.slider)
        slider.itemAdapter.add(
            PrimaryDrawerItem().apply { iconicsIcon = FontAwesome.Icon.faw_video; nameText = "Scenes"; identifier = 3 },
            PrimaryDrawerItem().apply { iconicsIcon = FontAwesome.Icon.faw_user_friends; nameText = "Actors"; identifier = 1 },
            PrimaryDrawerItem().apply { iconicsIcon = FontAwesome.Icon.faw_folder_open; nameText = "Albums"; identifier = 2 }
        )
        slider.setSelection(1)

        slider.onDrawerItemClickListener = { v, drawerItem, position ->
            while (navController.currentBackStackEntry != null)
                navController.popBackStack()

            if (drawerItem.identifier == 1L) {
                val bundle = bundleOf("showAlbums" to false)
                navController.navigate(R.id.actorsListFragment, bundle)
            }
            else if (drawerItem.identifier == 2L) {
                val bundle = bundleOf("showAlbums" to true)
                navController.navigate(R.id.actorsListFragment, bundle)
            }
            else if (drawerItem.identifier == 3L) {
                val bundle = bundleOf()
                navController.navigate(R.id.actorVideosFragment, bundle)
            }

            false
        }

        model.serverStatus.observe(this, Observer<ServerStatus> { status ->
            if (status == ServerStatus.ONLINE) {
                optionsMenu?.findItem(R.id.server_status)?.setIcon(R.drawable.ic_server_on)
                optionsMenu?.findItem(R.id.server_status)?.title = "Server online"
            }
            else if (status == ServerStatus.UNKNOWN) {
                optionsMenu?.findItem(R.id.server_status)?.setIcon(R.drawable.ic_server_unknown)
                optionsMenu?.findItem(R.id.server_status)?.title = "Server status unknown"
            }
            else {
                optionsMenu?.findItem(R.id.server_status)?.setIcon(R.drawable.ic_server_off)
                optionsMenu?.findItem(R.id.server_status)?.title = "Server offline"
            }
        })

        model.loggedIn.observe(this, Observer<Boolean> {  loggedIn ->
            optionsMenu?.findItem(R.id.server_status)?.isVisible = loggedIn
            optionsMenu?.findItem(R.id.action_random_video)?.isVisible = loggedIn
        })
    }

    fun onFragmentCreated(fragment : Fragment) {
        currentFragment = fragment

        optionsMenu?.findItem(R.id.filter_videos)?.isVisible = fragment is ActorVideosFragment
        optionsMenu?.findItem(R.id.sort_videos)?.isVisible = fragment is ActorVideosFragment
    }

    fun onLoginSuccessful() {
        model.loggedIn.postValue(true)
        queryStats()
        downloadImages()
        model.updateServerStatus(ServerStatus.UNKNOWN)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.childFragmentManager.backStackEntryCount == 0)
            AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", { dialog, which -> finish() })
                .setNegativeButton("No", null)
                .show()
        else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu
        model.loggedIn.postValue(model.loggedIn.value)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_random_video) {
            val video = model.randomVideo()

            if (video != null) {
                val bundle = bundleOf("videoId" to video.id)
                findNavController(R.id.nav_host_fragment).navigate(R.id.videoPlayer, bundle)
            }
        }
        else if (item.itemId == R.id.filter_videos) {
            val frag = VideoFilterFragment(currentFragment as ActorVideosFragment)
            frag.show(supportFragmentManager, "video_filter")
        }
        else if (item.itemId == R.id.sort_videos) {
            val frag = VideoSortFragment(currentFragment as ActorVideosFragment)
            frag.show(supportFragmentManager, "video_sorter")
        }

        return super.onOptionsItemSelected(item)
    }

    fun update() {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.fragments?.forEach {
            if (it is ActorListFragment)
                it.update()

            if (it is ActorVideosFragment)
                it.update()

            if (it is ActorImagesFragment)
                it.update()
        }
    }

    fun createDownloadingNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chanelId = "3000"
            val name = "Downloading status"
            val description = "Background image downloading status"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(chanelId, name, importance)
            mChannel.description = description
            mChannel.enableLights(false)
            notificationManager!!.createNotificationChannel(mChannel);
            notificationBuilder = Notification.Builder(this, chanelId)
        }
        else
            notificationBuilder = Notification.Builder(applicationContext)

        notificationBuilder!!.setOngoing(true)
            .setContentTitle("Downloading images")
            .setContentText("0%")
            .setSmallIcon(R.drawable.ic_download)
            .setProgress(100, 0, false)

        val notification: Notification = notificationBuilder!!.build()
        notificationManager!!.notify(downloadingProgressNotificationId, notification)
    }

    fun downloadImages() {
        if (model.downloadingImage)
            return

        val onImageDownloaded = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                update();

                if (System.currentTimeMillis() - previousImageLoadedTime > 1000) {
                    model.pvData.saveData()
                    previousImageLoadedTime = System.currentTimeMillis()
                }
            }
        }

        createDownloadingNotification()

        imageDownloadingHandler.post {
            model.downloadingImage = true
            downloadImages(this, onImageDownloaded, model.pvData.images.count {it.value.loaded})
            model.pvData.saveData()
            model.downloadingImage = false
            notificationManager?.cancel(downloadingProgressNotificationId)
            Log.d("PV", "Downloading images finished")
        }
    }
    
    fun queryStats() {
        model.startupRequestFinished = false
        model.startupRequestBeginTime = System.currentTimeMillis()
        statQuery(this)
    }

    fun parseStats(response : String) {
        try {
            val json = JSONObject(response)
            model.numActors = json.getJSONObject("data").getInt("numActors")
            model.numScenes = json.getJSONObject("data").getInt("numScenes")

            model.updateServerStatus(ServerStatus.ONLINE)
            queryTopActors()
        } catch (e: Throwable) {
            Log.d("PV", "Error while parsing stats: " + e.message)
            Log.d("PV", "Response:" + response)
        }
    }

    fun queryTopActors() {
        if (topActorsQueryTriesLeft > 0) {
            topActorsQuery(this, model.numActors)
            topActorsQueryTriesLeft--
        }
        else
            Log.d("PV", "Top actor query: Stopped requesting, out tries")
    }

    fun parseTopActorsResponse(actorsResponse : String) {
        try {
            val actorsJson = JSONObject(actorsResponse).getJSONObject("data")
                    .getJSONArray("topActors")

            var iterator = model.pvData.actors.iterator()
            while (iterator.hasNext()) {
                val act = iterator.next()
                var found = false

                for (i in 0 until actorsJson.length())
                    if (actorsJson.getJSONObject(i).getString("_id") == act.key)
                        found = true

                if (!found) //TODO : don't remove if has videos downloaded
                    iterator.remove()
            }

            for (i in 0 until actorsJson.length()) {
                val actorJson = actorsJson.getJSONObject(i)

                model.pvData.parseActor(actorJson)
            }

            update()
            model.pvData.saveData()
            topActorsQueryTriesLeft = queryMaxTries
            model.updateServerStatus(ServerStatus.ONLINE)
            queryVideos(null)
            //downloadImages()
        }
        catch (e: Throwable) {
            Log.d("PV", "Error while parsing top actors answer: " + e.message)
            e.printStackTrace()
            Log.d("PV", "Response:" + actorsResponse)
            queryTopActors()
        }
    }

    fun queryVideos(actor : Actor?, page : Int = 0) {
        val scenes = if (actor == null) model.numScenes else actor.videos.size
        val take = if (actor == null) 100 else scenes

        if (page * take < scenes) {
            if (videosQueryTriesLeft > 0) {
                videosQuery(
                    this,
                        min(take, scenes - page * take),
                        page,
                        actor
                )

                videosQueryTriesLeft--
            }
            else
                Log.d("PV", "Videos query: Stopped requesting, out tries ")
        }
        else {
            if (!model.startupRequestFinished) {
                model.startupRequestFinished = true
                Log.d(
                    "PV",
                    "Startup request finished in %.2f s".format((System.currentTimeMillis() - model.startupRequestBeginTime).toDouble() / 1000)
                )
            }
        }
    }

    fun parseVideosResponse(actor : Actor?, page : Int, response : String) {
        try {
            val json = JSONObject(response).getJSONObject("data").getJSONObject("getScenes")
            val videosJson = json.getJSONArray("items")

            //Remove deleted video if it wasn't loaded
            if (actor != null) {
                var iterator = actor.videos.iterator()
                while (iterator.hasNext()) {
                    val vid = iterator.next()
                    var found = false

                    for (i in 0 until videosJson.length())
                        if (videosJson.getJSONObject(i).getString("_id") == vid)
                            found = true

                    if (!found && !model.pvData.videos[vid]!!.loaded) {
                        model.pvData.videos.remove(vid)
                        iterator.remove()
                    }
                }
            }

            for (i in 0 until videosJson.length()) {
                val vid = videosJson.getJSONObject(i)
                model.pvData.parseVideo(vid)
            }
            update()

            model.pvData.saveData()
            model.updateServerStatus(ServerStatus.ONLINE)
            videosQueryTriesLeft = queryMaxTries
            queryVideos(actor, page + 1)
            downloadImages()
        }
        catch (e : Throwable) {
            Log.d("PV", "Error while parsing actor videos answer: " + e.message)
            Log.d("PV", "Response:" + response)
            queryVideos(actor, page)
        }
    }

    fun queryActorImages(actor : Actor) {
        actorImagesQuery(this, actor)
    }

    fun parseImagesResponse(actor : Actor, response : String) {
        try {
            val json = JSONObject(response).getJSONObject("data").getJSONObject("getImages")
            val imagesJson = json.getJSONArray("items")

            var iterator = actor.images.iterator()
            while (iterator.hasNext()) {
                val img = iterator.next()
                var found = false

                for (i in 0 until imagesJson.length())
                    if (imagesJson.getJSONObject(i).getString("_id") == img)
                        found = true

                if (!found && !model.pvData.images[img]!!.loaded) {
                    model.pvData.images.remove(img)
                    iterator.remove()
                }
            }

            for (i in 0 until imagesJson.length())
                model.pvData.parseImage(imagesJson.getJSONObject(i))

            update()
            model.pvData.saveData()
            model.updateServerStatus(ServerStatus.ONLINE)

            downloadImages()
        }
        catch (e : Throwable) {
            Log.d("PV", "Error while parsing actor images answer: " + e.message)
            Log.d("PV", "Response:" + response)
        }
    }

    fun onNetworkError(error : String) {
        model.updateServerStatus(ServerStatus.OFFLINE)

        model.startupRequestFinished = true
        Log.d("PV","Server seems to be down: " + error)
    }
}