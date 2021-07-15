package com.th.pv

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.th.pv.data.ActorVideo
import com.th.pv.data.PVData
import kotlin.random.Random

class PVViewModel : ViewModel() {
    lateinit var pvData : PVData
    var serverStatus = MutableLiveData<ServerStatus>(ServerStatus.UNKNOWN)
    var downloadingImage = false

    var numActors = 0
    var numScenes = 0
    var startupRequestBeginTime : Long = 0
    var startupRequestFinished = true
    var loggedIn = MutableLiveData<Boolean>(false)

    private var initialized = false

    fun load(context : Context) {
        if (!initialized) {
            initialized = true

            pvData = PVData(context.getExternalFilesDir(null)!!.absolutePath)
            pvData.readData()
        }
    }

    fun updateServerStatus(online : ServerStatus) {
        serverStatus.postValue(online)
    }

    fun randomVideo() : ActorVideo? {
        val keys = pvData.videos.keys.filter {
            if (serverStatus.value == ServerStatus.ONLINE) {
                val vid = pvData.videos[it]!!
                var score = vid.rating.toFloat()

                if (vid.meta.height >= 1080) score += 7
                else if (vid.meta.height >= 720) score += 5
                else if (vid.meta.height >= 480) score += 3
                else score += 1

                Random.nextFloat() < score / 17
            } else
                pvData.videos[it]!!.loaded
        }

        if (keys.isEmpty())
            return null

        return pvData.videos[keys.random()]
    }
}