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

    override fun toString() : String {
        var str = if (duration == null)
            "Duration: N/A"
        else
            "Duration: " + DateUtils.formatElapsedTime(duration!!.toLong())

        str += "\nDimensions: " + width + "x" + height
        str += "\nFramerate: %.2f".format(Locale.US, fps) + " fps"

        str += if (size == null)
            "\nSize: N/A"
        else
            "\nSize: " + size!! / 1024 / 1024 + " MB"

        str += if (bitrate == null)
            "\nBitrate: N/A"
        else
            "\nBitrate: " + bitrate!! / 1024 + " KBps"

        return str
    }
}