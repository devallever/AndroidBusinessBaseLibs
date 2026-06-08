package app.allever.android.lib.imageloader.engine.coil

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.widget.ImageView
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.imageloader.core.ILoader
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.load
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import java.io.File

object CoilLoader : ILoader {

    private val TAG = "ImageLoader-GlideLoader"

    override fun init(context: Context) {
        if (context is Application) {
            Coil.setImageLoader(context.initCoil())
        }
    }

    override fun load(resource: Any, imageView: ImageView, errorResId: Int?, placeholder: Int?) {
        imageView.load(resource) {
            applyDefault(this, errorResId, placeholder)
        }
    }

    override fun loadCircle(
        resource: Any,
        imageView: ImageView,
        borderWidthDp: Int?,
        borderColor: Int?,
        errorResId: Int?,
        placeholder: Int?
    ) {
        imageView.load(resource) {
            applyDefault(this, errorResId, placeholder)
            transformations(
                BorderCircleTransformation(
                    DisplayHelper.dip2px(borderWidthDp ?: 0),
                    borderColor ?: Color.parseColor("#00000000")
                )
            )
        }
    }

    override fun loadRound(
        resource: Any,
        imageView: ImageView,
        radiusDp: Float?,
        errorResId: Int?,
        placeholder: Int?
    ) {
        imageView.load(resource) {
            applyDefault(this, errorResId, placeholder)
            transformations(
                RoundedCornersTransformation(
                    radius = DisplayHelper.dip2px(radiusDp ?: 0f).toFloat()
                )
            )
        }
    }

    override fun loadGif(resource: Any, imageView: ImageView) {
        load(resource, imageView, null, null)
    }

    override fun loadBlur(resource: Any, imageView: ImageView, radius: Float?) {
        imageView.load(resource) {
            transformations(BlurTransformation(imageView.context, radius ?: 0f))
        }
    }

    override fun download(url: String, block: ((Boolean, File?) -> Unit)) {

    }


    private fun applyDefault(
        imageRequestBuilder: ImageRequest.Builder,
        errorResId: Int?,
        placeholder: Int?
    ): ImageRequest.Builder {
        errorResId?.let {
            imageRequestBuilder.error(it)
        }
        placeholder?.let {
            imageRequestBuilder.placeholder(it)
        }
        imageRequestBuilder.crossfade(true)
        return imageRequestBuilder
    }
}

fun Application.initCoil(): ImageLoader {
    return ImageLoader.Builder(this)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }

            add(SvgDecoder.Factory())
//            add(VideoFrameDecoder.Factory())
        }
        .crossfade(true)
        .build()
}
