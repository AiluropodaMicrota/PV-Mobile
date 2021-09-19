package com.th.pv.data

data class VideoSort(
    var type : Type = Type.RATING,
    var ascending : Boolean = true
) {
    enum class Type {
        RATING, DURATION, ALPHABETICAL, RESOLUTION, SIZE, ADDED_ON, LAST_VIEWED, VIEWS, RANDOM
    }

    fun comparator(pvData: PVData) : Comparator<String> {
        return Comparator { s1, s2 ->
            val v1 = pvData.videos[s1]!!
            val v2 = pvData.videos[s2]!!

            when {
                type == Type.RATING -> compareValues(v1.rating, v2.rating)
                type == Type.DURATION -> compareValues(v1.meta.duration, v2.meta.duration)
                type == Type.ALPHABETICAL -> compareValues(v1.name, v2.name)
                type == Type.RESOLUTION && compareValues(v1.meta.width, v2.meta.width) != 0 -> compareValues(v1.meta.width, v2.meta.width)
                type == Type.RESOLUTION && compareValues(v1.meta.width, v2.meta.width) == 0 -> compareValues(v1.meta.height, v2.meta.height)
                type == Type.SIZE -> compareValues(v1.meta.size, v2.meta.size)
                type == Type.ADDED_ON -> compareValues(v1.addedOn, v2.addedOn)
                type == Type.LAST_VIEWED -> compareValues(v1.watches.max(), v2.watches.max())
                type == Type.VIEWS -> compareValues(v1.watches.size, v2.watches.size)
                else -> 0
            }
        }
    }
}