package com.th.pv.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class Actor(
    val pvData: PVData,

    val id : String,
    var name: String,
    var views: Int, //TODO
    var score : Double,
    var rating : Double,
    var averageRating : Double,
    var avatar: String?,
    var thumbnail: String?,
    var videos : MutableList<String> = mutableListOf(),
    var images : MutableList<String> = mutableListOf(),
    var actorLabels : MutableList<String> = mutableListOf()
    ) {

    fun isAlbum() : Boolean {
        for (label in actorLabels)
            if (pvData.labels[label]!!.name.toLowerCase(Locale.ENGLISH) == "album")
                return true

        return false
    }

    fun toJson() : JSONObject {
        val obj = JSONObject()
        obj.put("_id", id)
        obj.put("score", score)
        obj.put("rating", rating)
        obj.put("averageRating", averageRating)
        obj.put("thumbnail", if (thumbnail == null) JSONObject.NULL else JSONObject().put("_id", thumbnail))
        obj.put("avatar", if (avatar == null) JSONObject.NULL else JSONObject().put("_id", avatar))
        obj.put("views", views) //TODO
        obj.put("name", name)
        obj.put("labels", JSONArray(actorLabels.map {  pvData.labels[it]!!.toJson()}))

        return obj
    }
}