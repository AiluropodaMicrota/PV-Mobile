package com.th.pv.data

import org.json.JSONObject

data class ActorVideoLabel (
        val id : String,
        var name : String
){
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("_id", id)
        obj.put("name", name)
        return obj
    }
}