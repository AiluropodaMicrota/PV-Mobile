package com.th.pv

import android.os.Handler
import android.util.Log
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.th.pv.actorVideos.ActorVideosFragment
import com.th.pv.data.Actor
import com.th.pv.data.ActorImage
import com.th.pv.data.ActorVideo
import com.th.pv.data.PVData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.net.URL


var httpClient = OkHttpClient()
var ip = ""
var password = ""

fun downloadVideo(activity: MainActivity, videosFragment: ActorVideosFragment, pvData: PVData, video: ActorVideo, callback: FutureCallback<File>) {
    video.loaded = false

    val url = pvData.getVideoPath(video)
    val f = File(pvData.savePath + "/videos/" +
            if (video.actors.isEmpty()) "_Other" else pvData.actors[video.actors[0]]!!.name
    ) //TODO: lookup all actors
    if (!f.exists())
        f.mkdirs()
    val path = f.absolutePath

    Ion.with(activity.applicationContext)
        .load(url)
        .progress{ downloaded, total ->
            activity.runOnUiThread {
                videosFragment.updateDownloadingProgress(downloaded, total)
            }
        }
        .write(File(path + "/file.part"))
        .setCallback { exception: Exception?, file: File? ->
            if (file != null) {
                video.loaded = true
                file.renameTo(File(pvData.getVideoPath(video)))
                callback.onCompleted(exception, file)
            }
            else
                callback.onCompleted(exception, file)
        }
}

fun downloadImages(activity: MainActivity, downloadFinishedHandler: Handler, imagesLoadedBeforeCount: Int) {
    var imageToDownload : ActorImage? = null
    var pvData = activity.model.pvData

    for ((key, value) in pvData.images) {
        if (!value.loaded && !value.loading) {
            imageToDownload = value
            break
        }
    }

    if (imageToDownload != null) {
        val url = ip + "/api/media/image/" + imageToDownload.id + "?password=" + password
        val f = File(pvData.getImagePath(imageToDownload))
        if (!f.exists())
            f.mkdirs()
        val path = f.absolutePath
        imageToDownload.loading = true

        URL(url).openStream().use { input ->
            FileOutputStream(File(path + "/" + imageToDownload.id + ".jpg.part")).use { output ->
                input.copyTo(output)
                val downloaded = File(path + "/" + imageToDownload.id + ".jpg.part")
                downloaded.renameTo(File(pvData.getImageSrc(imageToDownload)))
                imageToDownload.loaded = true
                downloadFinishedHandler.sendEmptyMessage(0)

                val downloadingProgress = (
                            (pvData.images.count {it.value.loaded}.toDouble() - imagesLoadedBeforeCount)
                            / (pvData.images.count() - imagesLoadedBeforeCount) * 100
                        ).toInt()
                activity.notificationBuilder?.setProgress(
                        100,
                        downloadingProgress,
                        false
                )
                activity.notificationBuilder?.setContentText("$downloadingProgress%")
                activity.notificationManager?.notify(activity.downloadingProgressNotificationId, activity.notificationBuilder?.build())

                downloadImages(activity, downloadFinishedHandler, imagesLoadedBeforeCount)
            }
        }
    }
}

fun postActorRating(activity: MainActivity, actor: Actor) {
    val body = "{\"operationName\":null,\"variables\":{\"ids\":[\"" + actor.id + "\"],\"opts\":{\"rating\":" + actor.rating + "}},\"query\":\"mutation (\$ids: [String!]!, \$opts: ActorUpdateOpts!) {\\n  updateActors(ids: \$ids, opts: \$opts) {\\n    rating\\n    __typename\\n  }\\n}\\n\"}"
    val url = ip + "/api/ql?password=" + password
    val client = OkHttpClient()
    val JSON = "application/json; charset=utf-8".toMediaType()
    val postBody = body.toRequestBody(JSON)

    val post = okhttp3.Request.Builder().url(url).post(postBody).build()
    httpClient.newCall(post).enqueue(object : Callback {
        override fun onResponse(call: Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                //Log.d("PV", "Actor rating post successfull")
            } else
                activity.model.updateServerStatus(ServerStatus.OFFLINE)
        }

        override fun onFailure(call: Call, e: IOException) {
            defaultOnFailure(activity, e)
        }
    })
}

fun actorImagesQuery(activity: MainActivity, actor: Actor) {
    val query = "query (\$query: ImageSearchQuery!) { getImages(query: \$query) { items { ...ImageFragment labels { _id name __typename } studio { _id name __typename } actors { ...ActorFragment avatar { _id color __typename } __typename } scene { _id name __typename } __typename } __typename }}fragment ImageFragment on Image { _id name bookmark favorite rating __typename}fragment ActorFragment on Actor { _id name description bornOn age aliases rating favorite bookmark customFields availableFields { _id name type values unit __typename } nationality { name alpha2 nationality __typename } __typename}"
    val variables = "{\"query\":{\"sortDir\":\"asc\",\"sortBy\":\"addedOn\",\"take\":10000,\"actors\":[\"" + actor.id + "\"]}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query + "&variables=" + variables

    val request = okhttp3.Request.Builder().url(url).build()
    httpClient.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                val body = response.body!!.string()

                activity.mHandler.post {
                    activity.parseImagesResponse(actor, body)
                }
            } else
                activity.model.updateServerStatus(ServerStatus.OFFLINE)
        }

        override fun onFailure(call: Call, e: IOException) {
            defaultOnFailure(activity, e)
        }
    })
}

fun videosQuery(activity: MainActivity, take: Int, page: Int, actor: Actor?) {
    val query = "query (\$query: SceneSearchQuery!, \$seed: String) {  getScenes(query: \$query, seed: \$seed) {    items { ...SceneFragment      actors {        ...ActorFragment        __typename      }  preview {      _id      meta {        dimensions {          width          height          __typename        }        __typename      }      __typename    }  markers {      _id      name      time      labels {        _id        name        color        __typename      }     thumbnail {        _id        __typename      }      __typename    }   studio {        ...StudioFragment        __typename      }      __typename    }    numItems    numPages    __typename  }}fragment SceneFragment on Scene {  _id  addedOn  name  releaseDate  description  rating  favorite  bookmark  studio {    _id    name    __typename  }  labels {    _id    name    color    __typename  }  thumbnail {    _id    color    __typename  }  meta {    size    duration  bitrate  fps    dimensions {      width      height      __typename    }    __typename  }  watches  streamLinks  path  customFields  availableFields {    _id    name    type    values    unit    __typename  }  __typename}fragment ActorFragment on Actor {  _id  name  description  bornOn  age  aliases  rating  favorite  bookmark  customFields  availableFields {    _id    name    type    values    unit    __typename  }  nationality {    name    alpha2    nationality    __typename  }  __typename}fragment StudioFragment on Studio {  _id  name  description  aliases  rating  favorite  bookmark  __typename}"
    var variables = "{\"query\":{\"query\":\"\",\"take\":" + take + ",\"page\":" + page + ",\"actors\":["
    variables += if (actor != null) "\""+ actor.id + "\"" else ""
    variables += "],\"include\":[],\"exclude\":[],\"sortDir\":\"desc\",\"sortBy\":\"relevance\",\"favorite\":false,\"bookmark\":false,\"rating\":0,\"durationMin\":null,\"durationMax\":null,\"studios\":null}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query + "&variables=" + variables

    val request = okhttp3.Request.Builder().url(url).build()

    val call = httpClient.newCall(request)
    call.enqueue(object : Callback {
        override fun onResponse(call: Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                val body = response.body!!.string()

                activity.mHandler.post {
                    activity.parseVideosResponse(actor, page, body)
                }
            } else
                activity.model.updateServerStatus(ServerStatus.OFFLINE)
        }

        override fun onFailure(call: Call, e: IOException) {
            defaultOnFailure(activity, e)
        }
    })
}

fun topActorsQuery(activity: MainActivity, take: Int) {
    val query = "{topActors(skip: 0, take: " + take + ") " +
            "{_id name rating averageRating score labels {_id name color __typename} thumbnail {_id __typename} avatar {_id __typename} __typename}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query

    val request = okhttp3.Request.Builder().url(url).build()
    httpClient.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                val body = response.body!!.string()

                activity.mHandler.post {
                    activity.parseTopActorsResponse(body)
                }
            } else
                activity.model.updateServerStatus(ServerStatus.OFFLINE)
        }

        override fun onFailure(call: Call, e: IOException) {
            defaultOnFailure(activity, e)
        }
    })
}

fun statQuery(activity: MainActivity) {
    val query = "query (\$query: MarkerSearchQuery!, \$seed: String) {%20 getMarkers(query: \$query, seed: \$seed) {%20%20%20%20 numItems%20 } numScenes numActors numMovies numImages}"
    val variables = "{\"query\":{\"query\":\"\"}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query + "&variables=" + variables

    val request = okhttp3.Request.Builder().url(url).build()

    httpClient.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                val body = response.body!!.string()

                activity.mHandler.post {
                    activity.parseStats(body)
                }
            } else
                activity.model.updateServerStatus(ServerStatus.OFFLINE)
        }

        override fun onFailure(call: Call, e: IOException) {
            defaultOnFailure(activity, e)
        }
    })
}

fun defaultOnFailure(activity: MainActivity, e: IOException) {
    val error = if (e.message != null) e.message!! else "No error message provided"

    activity.mHandler.post {
        activity.onNetworkError(error)
    }
}
