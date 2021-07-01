package com.th.pv

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.th.pv.data.ActorImage
import com.th.pv.data.PVData

fun glideLoadInto(context: Context, imageView : ImageView, pvData: PVData, image : ActorImage?) {
    if (image != null) {
        if (image.loaded) {
            val img = pvData.getImageSrc(image)

            Glide.with(context).load(img).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background).into(imageView)
        }
        else
            Glide.with(context).load(R.drawable.image_downloading).into(imageView)
    }
    else {
        Glide.with(context).load(R.drawable.image_not_avaliable).into(imageView)
    }
}

//Is there any way of doing this without copying?
fun glideLoadInto(context: View, imageView : ImageView, pvData: PVData, image : ActorImage?) {
    if (image != null) {
        if (image.loaded) {
            val img = pvData.getImageSrc(image)

            Glide.with(context).load(img).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background).into(imageView)
        }
        else
            Glide.with(context).load(R.drawable.image_downloading).into(imageView)
    }
    else {
        Glide.with(context).load(R.drawable.image_not_avaliable).into(imageView)
    }
}