package com.allever.video.editor.utils

import android.graphics.Rect
import android.graphics.RectF

fun RectF.rectCenterExpansion(bound: Float): RectF {
    this.left -= bound
    this.right += bound
    this.top -= bound
    this.bottom += bound
    return this
}

fun Rect.rectCenterExpansion(bound: Int): Rect {
    this.left -= bound
    this.right += bound
    this.top -= bound
    this.bottom += bound
    return this
}
