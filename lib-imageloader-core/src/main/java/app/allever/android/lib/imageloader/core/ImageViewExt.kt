package app.allever.android.lib.imageloader.core

import android.graphics.Color
import android.widget.ImageView
import java.io.File

fun ImageView.load(
    resource: Any,
    loadOrigin: Boolean = true,
    errorResId: Int? = ImageLoaderCore.errorResId(),
    placeholder: Int? = ImageLoaderCore.placeholder()
) {
    ImageLoaderCore.load(resource, this, loadOrigin, errorResId, placeholder)
}

fun ImageView.loadGif(resource: Any) {
    ImageLoaderCore.loadGif(resource, this)
}

fun ImageView.loadCircle(
    any: Any,
    borderWidth: Int = 0,
    borderColor: Int? = Color.parseColor("#00000000"),
    loadOrigin: Boolean = true,
    errorResId: Int? = ImageLoaderCore.errorResId(),
    placeholder: Int? = ImageLoaderCore.placeholder()
) {
    ImageLoaderCore.loadCircle(any, this, borderWidth, borderColor, loadOrigin, errorResId, placeholder)
}

fun ImageView.loadRound(
    any: Any,
    radius: Float = 8f,
    loadOrigin: Boolean = true,
    errorResId: Int? = ImageLoaderCore.errorResId(),
    placeholder: Int? = ImageLoaderCore.placeholder()
) {
    ImageLoaderCore.loadRound(any, this, radius, loadOrigin, errorResId, placeholder)
}

fun ImageView.loadBlur(any: Any, radius: Float = 10f, loadOrigin: Boolean = true) {
    ImageLoaderCore.loadBlur(any, this, radius, loadOrigin)
}

suspend fun downloadImg(any: String, block: (success: Boolean, file: File?) -> Unit) {
    ImageLoaderCore.download(any, block)
}
