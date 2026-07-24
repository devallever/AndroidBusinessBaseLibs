package app.allever.android.lib.camera.core

import android.os.Environment
import java.io.File

class CameraConfig private constructor(
    val cameraFacing: CameraFacing,
    val aspectRatio: AspectRatio,
    val flashMode: FlashMode,
    val videoQuality: VideoQuality,
    val photoSavePath: String,
    val videoSavePath: String,
    val maxVideoDuration: Long,
    val photoSize: Pair<Int, Int>?,
    val videoFrameRate: Int
) {

    class Builder {
        private var cameraFacing: CameraFacing = CameraFacing.FACE_BACK
        private var aspectRatio: AspectRatio = AspectRatio.RATIO_16_9
        private var flashMode: FlashMode = FlashMode.OFF
        private var videoQuality: VideoQuality = VideoQuality.HD_720P
        private var photoSavePath: String = defaultPhotoPath()
        private var videoSavePath: String = defaultVideoPath()
        private var maxVideoDuration: Long = 0L
        private var photoSize: Pair<Int, Int>? = null
        private var videoFrameRate: Int = 30

        fun setCameraFacing(cameraFacing: CameraFacing): Builder {
            this.cameraFacing = cameraFacing
            return this
        }

        fun setAspectRatio(aspectRatio: AspectRatio): Builder {
            this.aspectRatio = aspectRatio
            return this
        }

        fun setFlashMode(flashMode: FlashMode): Builder {
            this.flashMode = flashMode
            return this
        }

        fun setVideoQuality(videoQuality: VideoQuality): Builder {
            this.videoQuality = videoQuality
            return this
        }

        fun setPhotoSavePath(path: String): Builder {
            this.photoSavePath = path
            return this
        }

        fun setVideoSavePath(path: String): Builder {
            this.videoSavePath = path
            return this
        }

        fun setMaxVideoDuration(durationMs: Long): Builder {
            this.maxVideoDuration = durationMs
            return this
        }

        fun setPhotoSize(width: Int, height: Int): Builder {
            this.photoSize = Pair(width, height)
            return this
        }

        fun setVideoFrameRate(frameRate: Int): Builder {
            this.videoFrameRate = frameRate
            return this
        }

        fun build(): CameraConfig {
            return CameraConfig(
                cameraFacing = cameraFacing,
                aspectRatio = aspectRatio,
                flashMode = flashMode,
                videoQuality = videoQuality,
                photoSavePath = photoSavePath,
                videoSavePath = videoSavePath,
                maxVideoDuration = maxVideoDuration,
                photoSize = photoSize,
                videoFrameRate = videoFrameRate
            )
        }
    }

    companion object {
        private fun defaultPhotoPath(): String {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "CameraCore"
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir.absolutePath
        }

        private fun defaultVideoPath(): String {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "CameraCore"
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir.absolutePath
        }
    }
}