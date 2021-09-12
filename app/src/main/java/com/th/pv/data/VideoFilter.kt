package com.th.pv.data

data class VideoFilter(
        var actorsOr : MutableList<String> = mutableListOf()
) {
    fun fits(video : ActorVideo) : Boolean {
        if (actorsOr.isEmpty())
            return true

        for (actor in actorsOr)
            if (video.actors.contains(actor))
                return true

        return false
    }
}
