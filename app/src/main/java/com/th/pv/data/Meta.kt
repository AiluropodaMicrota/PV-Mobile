package com.th.pv.data

import android.text.format.DateUtils
import org.json.JSONObject
import java.util.*

data class Meta(
    var width : Int,
    var height : Int,
    var size : Long?,
    var duration: Double?,
    var fps : Double?,
    var bitrate : Int?
) {
    fun toJson() : JSONObject {
        val obj = JSONObject()
        obj.put("dimensions", JSONObject().put("width", width).put("height", height))
        obj.put("size", size)
        obj.put("duration", duration)
        obj.put("fps", fps)
        obj.put("bitrate", bitrate)
        return obj
    }
}