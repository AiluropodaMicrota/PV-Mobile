package com.th.pv.data

import org.json.JSONObject

data class ActorVideoMarker(
    val id : String,
    var name : String,
    var time : Int
) {

    fun toJson() : JSONObject {
        val obj = JSONObject()
        obj.put("_id", id)
        obj.put("name", name)
        obj.put("time", time)
        return obj
    }
}