package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.BaseVideoPlayer
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.IjkPlayerKernal

class IjkVideoPlayer: BaseVideoPlayer() {
    override var engine: IPlayerKernal<*> = IjkPlayerKernal().apply {
        registerListener(engineListener)
    }

    override fun detach() {
       super.detach()
        try {
            // 清除 Surface 引用（防止内存泄漏和渲染异常）
            engine.setSurface(null)
        } catch (e: Exception) {
           log(TAG, "detach error: ${e.message}")
        }
    }

    // ==================== 公共查询接口 ====================

    val tcpSpeed: Long
        get() = try { engine.getTcpSpeed() } catch (_: Exception) { 0L }

}
