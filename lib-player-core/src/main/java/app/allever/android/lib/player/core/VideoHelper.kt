package app.allever.android.lib.player.core

import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.VideoView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log

object VideoHelper {

    /**
     * 调整 RenderView 布局尺寸以适应视频宽高比
     * 适配 VideoView、SurfaceView、TextureView
     */
    fun adjustRenderViewLayout(renderView: View?, videoWidth: Int, videoHeight: Int, videoScaleMode: VideoScaleMode) {
        val renderView = renderView ?: return
        if (videoWidth <= 0 || videoHeight <= 0) return

        val parent = renderView.parent as? android.view.ViewGroup ?: return

        App.mainHandler.post {
            val tag = renderView.javaClass.simpleName
            val containerWidth = parent.width
            val containerHeight = parent.height
            if (containerWidth <= 0 || containerHeight <= 0) {
                // 视频尺寸未知，等 onVideoSizeChanged 回调时自动调整
                log(tag, "videoScaleMode changed to $videoScaleMode, but video size unknown, will adjust later")
                return@post
            }

            val (targetWidth, targetHeight) = calculateTargetSize(
                videoWidth, videoHeight,
                containerWidth, containerHeight,
                videoScaleMode
            )

            log(tag, "adjustRenderViewLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            val params = renderView.layoutParams
            params.width = targetWidth
            params.height = targetHeight

            if (params is android.widget.FrameLayout.LayoutParams) {
                params.gravity = android.view.Gravity.CENTER
            }

            renderView.layoutParams = params
        }
    }

    /**
     * 根据缩放模式计算目标尺寸
     *
     * @param videoW 视频宽度
     * @param videoH 视频高度
     * @param containerW 容器宽度
     * @param containerH 容器高度
     * @param mode 缩放模式
     * @return Pair<目标宽度, 目标高度>
     */
    private fun calculateTargetSize(
        videoW: Int, videoH: Int,
        containerW: Int, containerH: Int,
        mode: VideoScaleMode
    ): Pair<Int, Int> {
        if (videoW <= 0 || videoH <= 0 || containerW <= 0 || containerH <= 0) {
            return Pair(containerW, containerH)
        }

        val videoRatio = videoW.toFloat() / videoH.toFloat()
        val containerRatio = containerW.toFloat() / containerH.toFloat()

        return when (mode) {
            VideoScaleMode.FIT_CENTER -> {
                // 保持比例，完整显示（可能有黑边）
                if (videoRatio > containerRatio) {
                    // 视频更宽，以宽度为准
                    Pair(containerW, (containerW / videoRatio).toInt())
                } else {
                    // 视频更高，以高度为准
                    Pair((containerH * videoRatio).toInt(), containerH)
                }
            }
            VideoScaleMode.CROP_CENTER -> {
                // 保持比例，填满容器（可能裁剪边缘）
                if (videoRatio > containerRatio) {
                    // 视频更宽，以高度为准（裁剪左右）
                    Pair((containerH * videoRatio).toInt(), containerH)
                } else {
                    // 视频更高，以宽度为准（裁剪上下）
                    Pair(containerW, (containerW / videoRatio).toInt())
                }
            }
            VideoScaleMode.STRETCH -> {
                // 拉伸填满容器（可能变形）
                Pair(containerW, containerH)
            }
        }
    }
}