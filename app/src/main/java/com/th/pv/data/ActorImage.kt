package com.th.pv.data

import org.json.JSONArray
import org.json.JSONObject

data class ActorImage(
        val pvData: PVData,

        val id : String,
        var name : String?,
        var type : String,
        var loaded : Boolean,
        var actors : MutableList<String> = mutableListOf(),
        var loading : Boolean = false
){
    fun toJson() : JSONObject {
        val obj = JSONObject()
        obj.put("_id", id)
        obj.put("name", name)
        obj.put("type", type)
        obj.put("loaded", loaded)

        val act = JSONArray()
        for (key in actors)
            if (pvData.actors[key] != null)
                act.put(JSONObject().put("_id", pvData.actors[key]!!.id))

        obj.put("actors", act)

        return obj
    }
}