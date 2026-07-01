package com.allever.video.editor.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

import java.util.concurrent.atomic.AtomicInteger

object ViewUtil {

    private val sNextGeneratedId = AtomicInteger(1)

    @SuppressLint("NewApi")
    fun generateViewId(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            while (true) {
                val result = sNextGeneratedId.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF)
                    newValue = 1 // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue))
                    return result
            }
        } else
            return View.generateViewId()
    }

    fun hasState(states: IntArray?, state: Int): Boolean {
        if (states == null)
            return false

        for (state1 in states)
            if (state1 == state)
                return true

        return false
    }

    fun setBackground(v: View, drawable: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.background = drawable
        } else {
            v.setBackgroundDrawable(drawable)
        }
    }


    /**
     * 获取某个View的正确的位置
     *
     * @param view
     * @return
     */
    fun getViewRectF(view: View): RectF {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val left = location[0]
        val top = location[1]
        return RectF(left.toFloat(), top.toFloat(), (left + view.width).toFloat(), (top + view.height).toFloat())
    }

    fun getViewRect(view: View): Rect {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val left = location[0]
        val top = location[1]
        return Rect(left, top, left + view.width, top + view.height)
    }

    /**
     * 获取当前显示图片的宽高
     *
     * @return
     */
    private fun getCurrentSize(view: ImageView): FloatArray {
        val result = floatArrayOf(0.0f, 0.0f)
        val mDrawable = view.drawable ?: return result
        val r = mDrawable.bounds
        //真实宽高
        val w = r.width()
        val h = r.height()
        val matrix1 = view.imageMatrix
        val values1 = FloatArray(10)
        matrix1.getValues(values1)
        //当前显示的宽高

        result[0] = w * values1[0]
        result[1] = h * values1[4]
        return result
    }

    /**
     * 获取当前图片显示的Bounds
     *
     * @return Rect
     */
    fun getDrawableRect(view: ImageView): RectF {
        val size = getCurrentSize(view)
        val viewWidth = view.width.toFloat()
        val viewHeight = view.height.toFloat()

        val bLeft: Int
        val bTop: Int
        val vLeft: Int
        val vTop: Int
        val location = IntArray(2)
        view.getLocationInWindow(location)
        vLeft = location[0]
        vTop = location[1]

        val widthDistance: Float
        val heightDistance: Float
        widthDistance = (viewWidth - size[0]) / 2
        heightDistance = (viewHeight - size[1]) / 2
        bLeft = (vLeft.toDouble() + widthDistance.toDouble() + 0.5).toInt()
        bTop = (vTop.toDouble() + heightDistance.toDouble() + 0.5).toInt()

        return RectF(bLeft.toFloat(), bTop.toFloat(), (bLeft.toFloat() + size[0] + 0.5f).toInt().toFloat(), (bTop.toFloat() + size[1] + 0.5f).toInt().toFloat())
    }

    fun clear(v: View) {
        androidx.core.view.ViewCompat.setAlpha(v, 1f)
        androidx.core.view.ViewCompat.setScaleY(v, 1f)
        androidx.core.view.ViewCompat.setScaleX(v, 1f)
        androidx.core.view.ViewCompat.setTranslationY(v, 0f)
        androidx.core.view.ViewCompat.setTranslationX(v, 0f)
        androidx.core.view.ViewCompat.setRotation(v, 0f)
        androidx.core.view.ViewCompat.setRotationY(v, 0f)
        androidx.core.view.ViewCompat.setRotationX(v, 0f)
        androidx.core.view.ViewCompat.setPivotY(v, (v.measuredHeight / 2).toFloat())
        androidx.core.view.ViewCompat.setPivotX(v, (v.measuredWidth / 2).toFloat())
        androidx.core.view.ViewCompat.animate(v).setInterpolator(null).setStartDelay(0)
    }

    class ViewCompat : androidx.core.view.ViewCompat() {
        companion object {

            private val SIXTY_FPS_INTERVAL = 1000 / 60

            fun postOnAnimation(view: View, runnable: Runnable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    postOnAnimationJellyBean(
                        view,
                        runnable
                    )
                } else {
                    view.postDelayed(runnable, SIXTY_FPS_INTERVAL.toLong())
                }
            }

            @TargetApi(16)
            private fun postOnAnimationJellyBean(view: View, runnable: Runnable) {
                view.postOnAnimation(runnable)
            }

            fun getPointerIndex(action: Int): Int {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    getPointerIndexHoneyComb(
                        action
                    )
                else
                    getPointerIndexEclair(
                        action
                    )
            }

            @TargetApi(Build.VERSION_CODES.ECLAIR)
            private fun getPointerIndexEclair(action: Int): Int {
                return action and MotionEvent.ACTION_POINTER_ID_MASK shr MotionEvent.ACTION_POINTER_ID_SHIFT
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            private fun getPointerIndexHoneyComb(action: Int): Int {
                return action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
            }
        }

    }
}
