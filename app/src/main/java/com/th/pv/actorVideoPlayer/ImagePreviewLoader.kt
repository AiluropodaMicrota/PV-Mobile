package com.th.pv.actorVideoPlayer

import android.R.attr
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition.ViewAdapter
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.th.pv.data.ActorVideo
import com.th.pv.data.PVData


class ImagePreviewLoader (
    var imageView : ImageView,
    var pvData: PVData,
    var thumbnail : ActorVideo.Preview
) : PreviewLoader
{
    var loaded = false

    override fun loadPreview(currentPosition: Long, duration: Long) {
        if (!loaded) {
            for (i in 0 until 100) {
                Glide.with(imageView)
                    .load(pvData.getImageSrc(pvData.images[thumbnail.id]!!))
                    .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                    .transform(GlideThumbnailTransformation((duration.toDouble() / 100 * i).toLong(), duration))
                    .preload()
            }

            loaded = true
        }

        Glide.with(imageView)
            .load(pvData.getImageSrc(pvData.images[thumbnail.id]!!))
            .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
            .transform(GlideThumbnailTransformation(currentPosition, duration))
            .into(imageView)
    }

    init {
        //imageView.layoutParams.width = (thumbnail.meta.width / 100 * scale).toInt()
        //imageView.layoutParams.height = (thumbnail.meta.height * scale).toInt()
        //imageView.requestLayout()
    }
}