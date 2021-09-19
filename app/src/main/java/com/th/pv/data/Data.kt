package com.th.pv.data

import android.util.Log
import com.th.pv.ip
import com.th.pv.password
import org.json.JSONException
import org.json.JSONObject
import java.io.File

data class PVData(
        val savePath: String,
        val actors : MutableMap<String, Actor> = mutableMapOf(),
        val images : MutableMap<String, ActorImage> = mutableMapOf(),
        val videos : MutableMap<String, ActorVideo> = mutableMapOf(),
        val markers : MutableMap<String, ActorVideoMarker> = mutableMapOf(),
        val labels : MutableMap<String, ActorVideoLabel> = mutableMapOf()
) {
    fun getVideo(filter : VideoFilter, sorter : VideoSort, position: Int) : ActorVideo {
        val vids = filterVideos(filter)
        vids.sortWith(sorter.comparator(this))
        if (!sorter.ascending)
            vids.reverse()
        
        return videos[vids[position]]!!
    }

    fun filterVideos(filter : VideoFilter) : MutableList<String> {
        val list = mutableListOf<String>()

        for (vid in videos.keys)
            if (filter.fits(videos[vid]!!))
                list.add(vid)

        list.sortBy { !videos[it]!!.loaded }

        return list
    }

    fun getImagePath(image : ActorImage): String {
        return savePath + "/" + image.type
    }

    fun getImageSrc(image : ActorImage): String {
        return getImagePath(image) + "/" + image.id + ".jpg"
    }

    fun getVideoPath(video : ActorVideo): String {
        if (video.loaded) {
            if (video.actors.isNotEmpty()) //TODO: look up all actors
                return savePath + "/videos/" + actors[video.actors[0]]!!.name + "/" + video.id + ".mp4"
            else
                return savePath + "/videos/_Other/" + video.id + ".mp4"
        }
        else
            return ip + "/api/media/scene/" + video.id + "?password=" + password
    }

    fun getOrAddImage(id: String, type: String): ActorImage {
        if (!images.containsKey(id)) {
            images[id] = ActorImage(this, id, null, type, false)
        }

        return images[id]!!
    }

    fun readData() {
        try {
            if (File(savePath + "/images.json").exists()
                && File(savePath + "/actors.json").exists()
                && File(savePath + "/videos.json").exists()
                && File(savePath + "/config.json").exists()
            ) {
                File(savePath + "/actors.json").inputStream().bufferedReader().use {
                    val actorsJson = JSONObject(it.readText())
                    actors.clear()

                    for (key in actorsJson.keys())
                        parseActor(actorsJson.getJSONObject(key))
                }

                File(savePath + "/images.json").inputStream().bufferedReader().use {
                    val imagesJson = JSONObject(it.readText())
                    images.clear()

                    for (key in imagesJson.keys())
                        parseImage(imagesJson.getJSONObject(key))
                }

                File(savePath + "/videos.json").inputStream().bufferedReader().use {
                    val videosJson = JSONObject(it.readText())
                    videos.clear()

                    for (key in videosJson.keys())
                        parseVideo(videosJson.getJSONObject(key))
                }

                File(savePath + "/config.json").inputStream().bufferedReader().use {
                    val json = JSONObject(it.readText())

                    ip = json.getString("ip")
                    password = json.getString("password")
                }
            }
        } catch (e: JSONException) {
            images.clear()
            actors.clear()
            videos.clear()
            markers.clear()
            labels.clear()

            Log.d("PV", "File parse error: " + e.message)
            e.printStackTrace()
        }
    }

    fun parseVideo(vid: JSONObject) {
        val id = vid.getString("_id")
        val actorsJSON = vid.getJSONArray("actors")
        val actorName = if (actorsJSON.length() > 0) actorsJSON.getJSONObject(0)
            .getString("_id") else "_Other" //TODO

        val thumbnail = if (!vid.isNull("thumbnail")) {
            getOrAddImage(
                vid.getJSONObject("thumbnail").getString("_id"),
                "videoThumbnail/" + actorName
            ).id
        }
        else null

        val preview = if (!vid.isNull("preview")) {
            parsePreview(vid.getJSONObject("preview"))
        }
        else null

        if (preview != null)
            getOrAddImage(preview.id, "videoPreview/" + actorName)

        //Add video to actors
        for (i in 0 until actorsJSON.length()) {
            val actorId = actorsJSON.getJSONObject(i).getString("_id")
            if (actors.contains(actorId)) { //TODO: add actor?
                val actor = actors[actorId]!!

                if (!actor.videos.contains(id))
                    actor.videos.add(id)
            }
        }

        //Create or modify video
        if (!videos.containsKey(id)) {
            videos[id] = ActorVideo(
                this,
                id,
                vid.getString("name"),
                thumbnail,
                preview,
                vid.getInt("rating"),
                parseMeta(vid.getJSONObject("meta")),
                vid.getBoolean("favorite")
            )
        } else {
            videos[id]!!.name = vid.getString("name")
            videos[id]!!.thumbnail = thumbnail
            videos[id]!!.preview = preview
            videos[id]!!.rating = vid.getInt("rating")
            videos[id]!!.meta = parseMeta(vid.getJSONObject("meta"))
            videos[id]!!.favorite = vid.getBoolean("favorite")
        }

        //Add actors to video
        videos[id]!!.actors.clear()
        for (i in 0 until actorsJSON.length())
            videos[id]!!.actors.add(actorsJSON.getJSONObject(i).getString("_id"))

        //Add markers to video
        val markersJson = vid.getJSONArray("markers")
        for (j in 0 until markersJson.length()) {
            val m = markersJson.getJSONObject(j)
            val mid = m.getString("_id")

            if (!markers.containsKey(mid)) {
                markers[mid] = ActorVideoMarker(
                    mid,
                    m.getString("name"),
                    m.getInt("time")
                )
            } else {
                markers[mid]!!.name = m.getString("name")
                markers[mid]!!.time = m.getInt("time")
            }

            if (!videos[id]!!.markers.contains(mid))
                videos[id]!!.markers.add(mid)
        }

        //Add labels to video
        val labelsJson = vid.getJSONArray("labels")
        for (j in 0 until labelsJson.length()) {
            val l = labelsJson.getJSONObject(j)
            val lid = l.getString("_id")

            if (!labels.containsKey(lid)) {
                labels[lid] = ActorVideoLabel(
                    lid,
                    l.getString("name")
                )
            } else {
                labels[lid]!!.name = l.getString("name")
            }

            if (!videos[id]!!.labels.contains(lid))
                videos[id]!!.labels.add(lid)
        }

        //Some additional info
        if (!vid.isNull("loaded"))
            videos[id]!!.loaded = vid.getBoolean("loaded")
    }

    fun parsePreview(preview : JSONObject) : ActorVideo.Preview {
        return ActorVideo.Preview(
            preview.getString("_id"),
            parseMeta(preview.getJSONObject("meta"))
        )
    }

    fun parseMeta(meta : JSONObject): Meta {
        return Meta(
            meta.getJSONObject("dimensions").getInt("width"),
            meta.getJSONObject("dimensions").getInt("height"),
            if (meta.isNull("size")) null else meta.getLong("size"),
            if (meta.isNull("duration")) null else meta.getDouble("duration"),
            if (meta.isNull("fps")) null else meta.getDouble("fps"),
            if (meta.isNull("bitrate")) null else meta.getInt("bitrate")
        )
    }

    fun parseActor(actorJson: JSONObject) {
        val actorId = actorJson.getString("_id")
        val actorName = actorJson.getString("name")
        val actorScore = actorJson.getDouble("score")
        val actorRating = actorJson.getDouble("rating")
        val actorAverageRating = actorJson.getDouble("averageRating")
        val actorThumbnail = if (!actorJson.isNull("thumbnail")) {
            getOrAddImage(
                actorJson.getJSONObject("thumbnail").getString("_id"),
                "actorThumbnail"
            ).id
        } else null
        val actorAvatar = if (!actorJson.isNull("avatar")) {
            getOrAddImage(actorJson.getJSONObject("avatar").getString("_id"), "actorAvatar").id
        } else null

        if (!actors.containsKey(actorId)) {
            actors[actorId] = Actor(
                this,
                actorId,
                actorName,
                0,
                actorScore,
                actorRating,
                actorAverageRating,
                actorAvatar,
                actorThumbnail
            )
        } else {
            actors[actorId]!!.name = actorName
            actors[actorId]!!.avatar = actorAvatar
            actors[actorId]!!.thumbnail = actorThumbnail
            actors[actorId]!!.score = actorScore
            actors[actorId]!!.rating = actorRating
            actors[actorId]!!.averageRating = actorAverageRating
        }

        if (actorThumbnail != null) {
            images[actorThumbnail]!!.actors.clear()
            images[actorThumbnail]!!.actors.add(actorId)
        }

        if (actorAvatar != null) {
            images[actorAvatar]!!.actors.clear()
            images[actorAvatar]!!.actors.add(actorId)
        }

        val labelsJson = actorJson.getJSONArray("labels")
        actors[actorId]!!.actorLabels.clear()
        for (j in 0 until labelsJson.length()) {
            val l = labelsJson.getJSONObject(j)
            val lid = l.getString("_id")

            if (!labels.containsKey(lid)) {
                labels[lid] = ActorVideoLabel(
                    lid,
                    l.getString("name")
                )
            } else {
                labels[lid]!!.name = l.getString("name")
            }

            if (!actors[actorId]!!.actorLabels.contains(lid))
                actors[actorId]!!.actorLabels.add(lid)
        }
    }

    fun parseImage(img: JSONObject) {
        val id = img.getString("_id")
        val actorsJSON = img.getJSONArray("actors")
        val actorName =
                if (actorsJSON.length() > 0)
                    actorsJSON.getJSONObject(0).getString("_id")
                else
                    "_Other" //TODO

        //Add image to actors
        for (i in 0 until actorsJSON.length()) {
            val actorId = actorsJSON.getJSONObject(i).getString("_id")
            if (actors.contains(actorId)) { //TODO: add actor?
                val actor = actors[actorId]!!

                if (!actor.images.contains(id))
                    actor.images.add(id)
            }
        }

        //Create of modify image
        if (!images.containsKey(id)) {
            images[id] = ActorImage(
                this,
                id,
                if (img.isNull("name")) null else img.getString("name"),
                "actorImages/" + actorName,
                false
            )
        } else {
            images[id]!!.name = img.getString("name")
        }

        //Add actors to image
        images[id]!!.actors.clear()
        for (i in 0 until actorsJSON.length())
            images[id]!!.actors.add(actorsJSON.getJSONObject(i).getString("_id"))

        //Some additional info
        if (!img.isNull("type"))
            images[id]!!.type = img.getString("type")

        if (!img.isNull("loaded"))
            images[id]!!.loaded = img.getBoolean("loaded")
    }

    fun saveData() {
        val json = JSONObject()
        for ((index, value) in actors)
            json.put(index, value.toJson())

        File(savePath).mkdirs()

        File(savePath + "/.nomedia").createNewFile()

        File(savePath + "/actors.json.temp").writeText(json.toString())

        File(savePath + "/images.json.temp").printWriter().use { out ->
            val json = JSONObject()
            for ((index, value) in images)
                json.put(index, value.toJson())
            out.println(json.toString())
        }

        File(savePath + "/videos.json.temp").printWriter().use { out ->
            val json = JSONObject()
            for ((index, value) in videos)
                json.put(index, value.toJson())
            out.println(json.toString())
        }

        File(savePath + "/config.json.temp").printWriter().use { out ->
            val json = JSONObject()
            json.put("ip", ip)
            json.put("password", password)
            out.println(json.toString())
        }

        File(savePath + "/actors.json.temp").renameTo(File(savePath + "/actors.json"))
        File(savePath + "/images.json.temp").renameTo(File(savePath + "/images.json"))
        File(savePath + "/videos.json.temp").renameTo(File(savePath + "/videos.json"))
        File(savePath + "/config.json.temp").renameTo(File(savePath + "/config.json"))
    }
}