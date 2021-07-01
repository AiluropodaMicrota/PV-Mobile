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
        obj.put("actors", JSONArray(actors.map { JSONObject().put("_id", pvData.actors[it]!!.id) }))

        return obj
    }
}