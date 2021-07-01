package com.th.pv

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.th.pv.actorVideos.ActorVideosFragment
import com.th.pv.data.Actor
import com.th.pv.data.ActorImage
import com.th.pv.data.ActorVideo
import com.th.pv.data.PVData
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL


var serverOnline = true
var ip = ""
var password = ""

fun downloadVideo(activity : MainActivity, videosFragment: ActorVideosFragment, pvData: PVData, video : ActorVideo, callback : FutureCallback<File>) {
    video.loaded = false

    val url = pvData.getVideoPath(video)
    val f = File(
        Environment.getExternalStorageDirectory().toString() +
                "/PV/videos/" +
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

fun downloadImages(pvData: PVData, downloadFinishedHandler : Handler) {
    var imageToDownload : ActorImage? = null

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
                downloadImages(pvData, downloadFinishedHandler)
            }
        }
    }
}

fun actorImagesQuery(actor : Actor, ctx : Context, responseListener: Response.Listener<String>) {
    val queue = Volley.newRequestQueue(ctx)
    val query = "query (\$query: ImageSearchQuery!) { getImages(query: \$query) { items { ...ImageFragment labels { _id name __typename } studio { _id name __typename } actors { ...ActorFragment avatar { _id color __typename } __typename } scene { _id name __typename } __typename } __typename }}fragment ImageFragment on Image { _id name bookmark favorite rating __typename}fragment ActorFragment on Actor { _id name description bornOn age aliases rating favorite bookmark customFields availableFields { _id name type values unit __typename } nationality { name alpha2 nationality __typename } __typename}"
    val variables = "{\"query\":{\"sortDir\":\"asc\",\"sortBy\":\"addedOn\",\"take\":10000,\"actors\":[\"" + actor.id + "\"]}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query + "&variables=" + variables

    val stringRequest = StringRequest(
        Request.Method.GET, url, responseListener,
        Response.ErrorListener {error->
            Log.d("PV","Actor images request failed: " + error)
        })
    stringRequest.retryPolicy = DefaultRetryPolicy( 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    queue.add(stringRequest)
}

fun videosQuery(take : Int, page : Int, actor : Actor?, ctx : Context, responseListener : Response.Listener<String>, errorListener : Response.ErrorListener) {
    val queue = Volley.newRequestQueue(ctx)
    val query = "query (\$query: SceneSearchQuery!, \$seed: String) {  getScenes(query: \$query, seed: \$seed) {    items { ...SceneFragment      actors {        ...ActorFragment        __typename      }  preview {      _id      meta {        dimensions {          width          height          __typename        }        __typename      }      __typename    }  markers {      _id      name      time      labels {        _id        name        color        __typename      }     thumbnail {        _id        __typename      }      __typename    }   studio {        ...StudioFragment        __typename      }      __typename    }    numItems    numPages    __typename  }}fragment SceneFragment on Scene {  _id  addedOn  name  releaseDate  description  rating  favorite  bookmark  studio {    _id    name    __typename  }  labels {    _id    name    color    __typename  }  thumbnail {    _id    color    __typename  }  meta {    size    duration  bitrate  fps    dimensions {      width      height      __typename    }    __typename  }  watches  streamLinks  path  customFields  availableFields {    _id    name    type    values    unit    __typename  }  __typename}fragment ActorFragment on Actor {  _id  name  description  bornOn  age  aliases  rating  favorite  bookmark  customFields  availableFields {    _id    name    type    values    unit    __typename  }  nationality {    name    alpha2    nationality    __typename  }  __typename}fragment StudioFragment on Studio {  _id  name  description  aliases  rating  favorite  bookmark  __typename}"
    var variables = "{\"query\":{\"query\":\"\",\"take\":" + take + ",\"page\":" + page + ",\"actors\":["
    variables += if (actor != null) "\""+ actor.id + "\"" else ""
    variables += "],\"include\":[],\"exclude\":[],\"sortDir\":\"desc\",\"sortBy\":\"relevance\",\"favorite\":false,\"bookmark\":false,\"rating\":0,\"durationMin\":null,\"durationMax\":null,\"studios\":null}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query + "&variables=" + variables

    val stringRequest = StringRequest(Request.Method.GET, url, responseListener, errorListener)
    stringRequest.retryPolicy = DefaultRetryPolicy( 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    queue.add(stringRequest)
}

fun topActorsQuery(take : Int, ctx : Context, responseListener : Response.Listener<String>, errorListener : Response.ErrorListener) {
    val queue = Volley.newRequestQueue(ctx)
    var query = "{topActors(skip: 0, take: " + take + ") " +
            "{_id name rating averageRating score labels {_id name color __typename} thumbnail {_id __typename} avatar {_id __typename} __typename}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query

    val stringRequest = StringRequest(Request.Method.GET, url, responseListener, errorListener)
    stringRequest.retryPolicy = DefaultRetryPolicy( 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    queue.add(stringRequest)
}

fun statQuery( ctx : Context, responseListener : Response.Listener<String>, errorListener : Response.ErrorListener) {
    val queue = Volley.newRequestQueue(ctx)
    var query = "query (\$query: MarkerSearchQuery!, \$seed: String) {%20 getMarkers(query: \$query, seed: \$seed) {%20%20%20%20 numItems%20 } numScenes numActors numMovies numImages}"
    var variables = "{\"query\":{\"query\":\"\"}}"

    val url = ip + "/api/ql?password=" + password + "&query=" + query + "&variables=" + variables

    val stringRequest = StringRequest(Request.Method.GET, url, responseListener, errorListener)
    stringRequest.retryPolicy = DefaultRetryPolicy( 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    queue.add(stringRequest)
}

