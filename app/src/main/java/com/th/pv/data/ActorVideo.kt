package com.th.pv.data

import android.text.format.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class ActorVideo(
    val pvData: PVData,

    val id : String,
    var name : String,
    var thumbnail : String?,
    var preview : Preview?,

    var rating : Int,
    var meta : Meta,
    var favorite : Boolean,
    var addedOn : Long,
    var watches : MutableList<Long> = mutableListOf(),
    var loaded : Boolean = false,
    var actors : MutableList<String> = mutableListOf(),
    var markers : MutableList<String> = mutableListOf(),
    var labels : MutableList<String> = mutableListOf()
) {
    data class Preview(
        var id : String,
        var meta : Meta
    ) {
        fun toJson() : JSONObject {
            val obj = JSONObject()
            obj.put("_id", id)
            obj.put("meta", meta.toJson())
            return obj
        }
    }

    fun isHorizontal() : Boolean {
        return meta.width > meta.height
    }

    fun toJson() : JSONObject {
        val obj = JSONObject()

        obj.put("_id", id)
        obj.put("name", name)
        obj.put("rating", rating)
        obj.put("favorite", favorite)
        obj.put("loaded", loaded)
        obj.put("addedOn", addedOn)
        obj.put("watches", JSONArray(watches))
        obj.put("actors", JSONArray(actors.map { JSONObject().put("_id", pvData.actors[it]!!.id) }))
        obj.put("labels", JSONArray(labels.map {  pvData.labels[it]!!.toJson()}))
        obj.put("markers", JSONArray(markers.map { pvData.markers[it]!!.toJson()  }))
        obj.put("thumbnail", JSONObject().put("_id", thumbnail))
        obj.put("meta", meta.toJson())
        obj.put("preview", preview?.toJson())

        return obj
    }

    override fun toString() : String {
        var str = if (meta.duration == null)
            "Duration: N/A"
        else
            "Duration: " + DateUtils.formatElapsedTime(meta.duration!!.toLong())

        str += "\nDimensions: " + meta.width + "x" + meta.height
        str += "\nFramerate: %.2f".format(Locale.US, meta.fps) + " fps"

        str += if (meta.size == null)
            "\nSize: N/A"
        else
            "\nSize: " + meta.size!! / 1024 / 1024 + " MB"

        str += if (meta.bitrate == null)
            "\nBitrate: N/A"
        else
            "\nBitrate: " + meta.bitrate!! / 1024 + " KBps"

        str += "\nViews: " + watches.size

        if (watches.size > 0)
            str += "\nLast time watched: " + SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Date(watches.max()))

        return str
    }
}