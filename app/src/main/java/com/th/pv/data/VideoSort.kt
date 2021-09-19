package com.th.pv.data

data class VideoSort(
    var type : Type = Type.RATING,
    var ascending : Boolean = true
) {
    enum class Type {
        RATING, DURATION
    }

    fun comparator(pvData: PVData) : Comparator<String> {
        return Comparator { s1, s2 ->
            val v1 = pvData.videos[s1]!!
            val v2 = pvData.videos[s2]!!

            when {
                type == Type.RATING -> compareValues(v1.rating, v2.rating)
                type == Type.DURATION -> compareValues(v1.meta.duration, v2.meta.duration)
                else -> 0
            }
        }
    }
}