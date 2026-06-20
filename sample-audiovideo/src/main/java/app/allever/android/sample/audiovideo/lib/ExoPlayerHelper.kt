package app.allever.android.sample.audiovideo.lib

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

object ExoPlayerHelper {

    /**
     * 应用视频缩放模式（PlayerView 版本）
     *
     * 通过 PlayerView 的 resizeMode 属性控制：
     * - FIT_CENTER → RESIZE_MODE_FIT（保持比例，完整显示）
     * - CROP_CENTER → RESIZE_MODE_ZOOM（保持比例，填满容器）
     * - STRETCH → RESIZE_MODE_FILL（拉伸填满）
     */
    @OptIn(UnstableApi::class)
    fun applyVideoScaleMode(playerView: PlayerView?, videoScaleMode: VideoScaleMode) {
        playerView?.resizeMode = when (videoScaleMode) {
            VideoScaleMode.FIT_CENTER -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            VideoScaleMode.CROP_CENTER -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            VideoScaleMode.STRETCH -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    }
}