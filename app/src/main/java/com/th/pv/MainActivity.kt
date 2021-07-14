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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
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
import com.th.pv.data.PVData
import org.json.JSONObject
import kotlin.math.min
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    val queryMaxTries = 10
    var downloadingProgressNotificationId = 100

    lateinit var pvData : PVData
    lateinit var optionsMenu : Menu
    var mHandler = Handler(Looper.getMainLooper())
    var notificationBuilder : Notification.Builder? = null
    var notificationManager : NotificationManager? = null

    private var downloadingImage = false
    private lateinit var imageDownloadingHandlerThread : HandlerThread
    private lateinit var imageDownloadingHandler : Handler
    private var previousImageLoadedTime : Long = 0

    var numActors = 0
    var numScenes = 0
    var topActorsQueryTriesLeft = queryMaxTries
    var videosQueryTriesLeft = queryMaxTries
    var startupRequestBeginTime : Long = 0
    var startupRequestFinished = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        pvData = PVData(applicationContext.getExternalFilesDir(null)!!.absolutePath)
        val navController = findNavController(R.id.nav_host_fragment)
        imageDownloadingHandlerThread = HandlerThread("ImageDownloadingHandlerThread")
        imageDownloadingHandlerThread.start()
        imageDownloadingHandler = Handler(imageDownloadingHandlerThread.looper)

        pvData.readData()

        Ion.getDefault(applicationContext).conscryptMiddleware.enable(false);

        findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)

        val slider = findViewById<MaterialDrawerSliderView>(R.id.slider)
        slider.itemAdapter.add(
            PrimaryDrawerItem().apply { iconicsIcon = FontAwesome.Icon.faw_user_friends; nameText = "Actors"; identifier = 1 },
            PrimaryDrawerItem().apply { iconicsIcon = FontAwesome.Icon.faw_folder_open; nameText = "Albums"; identifier = 2 }
        )
        slider.setSelection(1)

        slider.onDrawerItemClickListener = { v, drawerItem, position ->
            if (drawerItem.identifier == 1L) {
                val bundle = bundleOf("showAlbums" to false)
                navController.popBackStack(R.id.actorsListFragment, true)
                navController.navigate(R.id.actorsListFragment, bundle)
            }
            else if (drawerItem.identifier == 2L) {
                val bundle = bundleOf("showAlbums" to true)
                navController.popBackStack(R.id.actorsListFragment, true)
                navController.navigate(R.id.actorsListFragment, bundle)
            }

            false
        }
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_random_video) {
            val key = pvData.videos.keys.filter {
                if (serverOnline) {
                    val vid = pvData.videos[it]!!
                    var score = vid.rating.toFloat()

                    if (vid.meta.height >= 1080) score += 7
                    else if (vid.meta.height >= 720) score += 5
                    else if (vid.meta.height >= 480) score += 3
                    else score += 1

                    Random.nextFloat() < score / 17
                } else
                    pvData.videos[it]!!.loaded
            }.random()

            val bundle = bundleOf("videoId" to pvData.videos[key]!!.id)
            findNavController(R.id.nav_host_fragment).navigate(R.id.videoPlayer, bundle)
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
        if (downloadingImage)
            return

        val onImageDownloaded = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                update();

                if (System.currentTimeMillis() - previousImageLoadedTime > 1000) {
                    pvData.saveData()
                    previousImageLoadedTime = System.currentTimeMillis()
                }
            }
        }

        createDownloadingNotification()

        imageDownloadingHandler.post {
            downloadingImage = true
            downloadImages(this, onImageDownloaded, pvData.images.count {it.value.loaded})
            pvData.saveData()
            downloadingImage = false
            notificationManager?.cancel(downloadingProgressNotificationId)
            Log.d("PV", "Downloading images finished")
        }
    }
    
    fun queryStats() {
        startupRequestFinished = false
        startupRequestBeginTime = System.currentTimeMillis()
        statQuery(this)
    }

    fun parseStats(response : String) {
        try {
            val json = JSONObject(response)
            numActors = json.getJSONObject("data").getInt("numActors")
            numScenes = json.getJSONObject("data").getInt("numScenes")

            updateServerStatus(true)
            queryTopActors()
        } catch (e: Throwable) {
            Log.d("PV", "Error while parsing stats: " + e.message)
            Log.d("PV", "Response:" + response)
        }
    }

    fun queryTopActors() {
        if (topActorsQueryTriesLeft > 0) {
            topActorsQuery(this, numActors)
            topActorsQueryTriesLeft--
        }
        else
            Log.d("PV", "Top actor query: Stopped requesting, out tries")
    }

    fun parseTopActorsResponse(actorsResponse : String) {
        try {
            val actorsJson = JSONObject(actorsResponse).getJSONObject("data")
                    .getJSONArray("topActors")

            var iterator = pvData.actors.iterator()
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

                pvData.parseActor(actorJson)
            }

            pvData.saveData()
            topActorsQueryTriesLeft = queryMaxTries
            updateServerStatus(true)
            queryVideos(null)
            //downloadImages()
        }
        catch (e: Throwable) {
            Log.d("PV", "Error while parsing top actors answer: " + e.message)
            //e.printStackTrace()
            Log.d("PV", "Response:" + actorsResponse)
            queryTopActors()
        }
    }

    fun queryVideos(actor : Actor?, page : Int = 0) {
        val scenes = if (actor == null) numScenes else actor.videos.size
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
            if (!startupRequestFinished) {
                startupRequestFinished = true
                Log.d(
                    "PV",
                    "Startup request finished in %.2f s".format((System.currentTimeMillis() - startupRequestBeginTime).toDouble() / 1000)
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

                    if (!found && !pvData.videos[vid]!!.loaded) {
                        pvData.videos.remove(vid)
                        iterator.remove()
                    }
                }
            }

            for (i in 0 until videosJson.length()) {
                val vid = videosJson.getJSONObject(i)
                pvData.parseVideo(vid)
            }
            update()

            pvData.saveData()
            updateServerStatus(true)
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

                if (!found && !pvData.images[img]!!.loaded) {
                    pvData.images.remove(img)
                    iterator.remove()
                }
            }

            for (i in 0 until imagesJson.length())
                pvData.parseImage(imagesJson.getJSONObject(i))

            update()
            pvData.saveData()

            downloadImages()
        }
        catch (e : Throwable) {
            Log.d("PV", "Error while parsing actor images answer: " + e.message)
            Log.d("PV", "Response:" + response)
        }
    }

    fun updateServerStatus(online : Boolean) {
        serverOnline = online

        if (serverOnline)
            optionsMenu.findItem(R.id.server_status).setIcon(R.drawable.ic_server_on)
        else
            optionsMenu.findItem(R.id.server_status).setIcon(R.drawable.ic_server_off)
    }

    fun onNetworkError(error : String) {
        updateServerStatus(false)

        startupRequestFinished = true
        Log.d("PV","Server seems to be down: " + error)
    }
}