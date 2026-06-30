package com.allever.video.editor.function.save

import android.graphics.Point
import android.graphics.Rect

/**
 */

class CombineBean {

    var type = TYPE_VIDEO
    /** 文件路径  */
    var file: String = ""

    var srcWidth: Int = 0
    var srcHeight: Int = 0
    /** 原视频播放时长，单位ms  */
    var srcDuring: Int = 0
    /** 是否静音播放  */
    var slient: Boolean = false

    /** 合成后坐标  */
    var pos: Point = Point()
    /** 缩放后大小（裁剪前）  */
    var scale: Float = 0.toFloat()
    /** 裁剪区域（缩放后的区域）  */
    var clipRect: Rect = Rect()

    var hasAudio: Boolean = false

    /**
     * 延时播放时间
     */
    var startOffset: Int = 0

    companion object {

        val TYPE_IMG = 1
        val TYPE_VIDEO = 2
    }
}
