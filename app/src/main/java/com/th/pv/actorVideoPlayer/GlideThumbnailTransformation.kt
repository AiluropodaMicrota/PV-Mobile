package com.th.pv.actorVideoPlayer

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.ByteBuffer
import java.security.MessageDigest

/*
 * Copyright 2017 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




class GlideThumbnailTransformation(position: Long, duration : Long) :
    BitmapTransformation() {
    private val x: Int

    override fun transform(
        pool: BitmapPool, toTransform: Bitmap,
        outWidth: Int, outHeight: Int
    ): Bitmap {
        val width = toTransform.width / MAX_COLUMNS
        return Bitmap.createBitmap(toTransform, x * width, 0, width, toTransform.height)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val data = ByteBuffer.allocate(8).putInt(x).array()
        messageDigest.update(data)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as GlideThumbnailTransformation
        return x == that.x
    }

    override fun hashCode(): Int {
        var result = x
        return result
    }

    companion object {
        private const val MAX_COLUMNS = 100
    }

    init {
        x = (position.toDouble() / duration * MAX_COLUMNS).toInt()
    }
}
