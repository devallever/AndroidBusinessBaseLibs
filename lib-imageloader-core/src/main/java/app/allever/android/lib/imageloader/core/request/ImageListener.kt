package app.allever.android.lib.imageloader.core.request

import android.graphics.Bitmap

/**
 * 图片加载事件监听
 */
interface ImageListener {

    /** 开始加载（显示占位图） */
    fun onStart() {}

    /** 加载成功，返回最终 Bitmap（已应用变换） */
    fun onSuccess(bitmap: Bitmap) {}

    /** 加载失败 */
    fun onError(error: Throwable) {}
}
