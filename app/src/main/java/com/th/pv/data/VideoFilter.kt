package com.th.pv.data

data class VideoFilter(
        var minRating : Int = 0,
        var onlyFavourite : Boolean = false,
        var actorsOr : MutableList<String> = mutableListOf(),
        var minDuration : Double = 0.0,
        var maxDuration: Double = 36000.0
) {
    fun fits(video : ActorVideo) : Boolean {
        return fitsActors(video) &&
                video.rating >= minRating &&
                (!onlyFavourite || video.favorite) &&
                (video.meta.duration == null || (video.meta.duration!! in minDuration..maxDuration))
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
