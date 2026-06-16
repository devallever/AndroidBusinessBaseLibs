package app.flash.tunnel.vpn.lib.common.ext

import android.graphics.Color
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation

fun ImageView.loadCircle(resource: Any) {
    Glide.with(context).load(resource).apply(
        RequestOptions.bitmapTransform(
            CropCircleWithBorderTransformation(
                0,
                Color.parseColor("#00000000")
            )
        )
    ).into(this)
}