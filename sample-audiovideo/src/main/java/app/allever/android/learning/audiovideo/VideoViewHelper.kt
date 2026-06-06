package app.allever.android.learning.audiovideo

import android.content.pm.ActivityInfo
import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.model.MediaItem

object VideoViewHelper {

    private fun checkVideoAvailable(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }

        val cr = App.context.contentResolver
        var cursor: Cursor? = null
        try {
            cursor = cr.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Video.VideoColumns._ID,
                    MediaStore.Video.VideoColumns.DATA,
                    MediaStore.Video.VideoColumns.DURATION
                ),
                MediaStore.Video.VideoColumns.DATA + " = ? ", arrayOf(path),
                MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC" + ", " + MediaStore.Video.VideoColumns._ID + " ASC"
            )

            if (cursor == null) {
                return false
            }

            if (cursor.moveToFirst()) {
                val durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
                //有些文件后缀为视频格式，却不是视频文件，长度为0， 需要排除
                val time = cursor.getLong(durationIndex)
                if (time <= 0) {
                    return false
                }
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cursor?.close()
            return false
        }

        return true
    }


    fun autoFixContainerSize(
        container: View,
        videoW: Int,
        videoH: Int,
        block: (renderW: Int, renderH:Int) -> Unit
    ) {
        container.post {
            val w: Float = videoW.toFloat()
            val h: Float = videoH.toFloat()
            val sw: Float = container.width.toFloat()
            val sh: Float = container.height.toFloat()

            log("显示视频尺寸: $videoW x $videoH")
            log("容器尺寸: $sw x $sh")

            var displayW = 0
            var displayH = 0

            log("video size = $w x $h")
            log("surface size = $sw x $sh")

            if (App.context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                log("竖屏")
                if (w > h) {
                    //横向视频：以屏幕宽度为基准，按比例缩放高度
                    displayW = sw.toInt()
                    displayH = (h * sw / w).toInt()
                } else {
                    //纵向视频
                    if (h > sh) {
                        // 超高视频（如长短视频）：以屏幕高度为基准，按比例缩放宽度
                        displayH = sh.toInt()
                        displayW = (w * sh / h).toInt()
                    } else {
                        // 正常纵向视频：以屏幕宽度为基准，按比例缩放高度
                        displayW = sw.toInt()
                        displayH = (h * sw / w).toInt()
                    }
                }

            } else {
                log("横屏")
                if (w > h) {
                    //横向视频
                    if (w > sw) {
                        //超宽视频（如21:9电影）：以屏幕宽度为基准，按比例缩放高度
                        displayW = sw.toInt()
                        displayH = (h * sw / w).toInt()
                    } else {
                        // 正常横向视频：以屏幕高度为基准，按比例缩放宽度
                        displayH = sh.toInt()
                        displayW = (w * sh / h).toInt()
                    }
                } else {
                    //纵向视频：以屏幕高度为基准，按比例缩放宽度
                    displayH = sh.toInt()
                    displayW = (w * sh / h).toInt()
                }
            }


            log("显示视频尺寸: $displayW x $displayH")
            block.invoke(displayW, displayH)

            //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
//            val params = binding.videoView.layoutParams
//            params.width = displayW
//            params.height = displayH
//            binding.videoView.layoutParams = params
        }
    }
}