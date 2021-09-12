package com.th.pv.data

data class VideoFilter(
        var minRating : Int = 0,
        var actorsOr : MutableList<String> = mutableListOf()
) {
    fun fits(video : ActorVideo) : Boolean {
        return fitsRating(video) && fitsActors(video)
    }





    fun fitsRating(video : ActorVideo) : Boolean {
        return  video.rating >= minRating
    }

    fun fitsActors(video : ActorVideo) : Boolean {
        if (actorsOr.isEmpty())
            return true

        for (actor in actorsOr)
            if (video.actors.contains(actor))
                return true

        return false
    }
}
