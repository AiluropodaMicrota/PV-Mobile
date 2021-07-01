package com.th.pv.data

import org.json.JSONArray
import org.json.JSONObject

data class ActorVideo(
    val pvData: PVData,

    val id : String,
    var name : String,
    var thumbnail : String?,
    var preview : Preview?,

    var rating : Int,
    var meta : Meta,
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
        obj.put("loaded", loaded)
        obj.put("actors", JSONArray(actors.map { JSONObject().put("_id", pvData.actors[it]!!.id) }))
        obj.put("labels", JSONArray(labels.map {  pvData.labels[it]!!.toJson()}))
        obj.put("markers", JSONArray(markers.map { pvData.markers[it]!!.toJson()  }))
        obj.put("thumbnail", JSONObject().put("_id", thumbnail))
        obj.put("meta", meta.toJson())
        obj.put("preview", preview?.toJson())

        return obj
    }
}