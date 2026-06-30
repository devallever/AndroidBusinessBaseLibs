package com.step.wincash.ui.widget

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.step.wincash.R

/**
 * 让手指跨 item 划过哪个格子，哪个格子就被刮——统一在 RecyclerView 层处理。
 */
class CrossItemScratchTouchListener(
    private val recyclerView: RecyclerView
) : RecyclerView.OnItemTouchListener {

    private var activeScratchView: ScratchCardView? = null

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val target = findScratchUnder(e) ?: return false
                activeScratchView = target
                val (sx, sy) = toLocalInScratch(target, e)
                target.beginScratch(sx, sy)
                rv.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // 若之前没拦截，但移动到了可刮区域，则从此刻开始拦截（支持“从外部滑入再刮”）
                if (activeScratchView == null) {
                    val target = findScratchUnder(e) ?: return false
                    activeScratchView = target
                    val (sx, sy) = toLocalInScratch(target, e)
                    target.beginScratch(sx, sy)
                    rv.requestDisallowInterceptTouchEvent(true)
                    return true
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                return activeScratchView != null
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        when (e.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val target = findScratchUnder(e)
                if (target == null) {
                    // 移动到空白（比如 item 间距）就结束当前笔
                    activeScratchView?.endScratch()
                    activeScratchView = null
                    return
                }
                if (target !== activeScratchView) {
                    // 跨 item：上一个收笔，新 item 起笔
                    activeScratchView?.endScratch()
                    activeScratchView = target
                    val (sx, sy) = toLocalInScratch(target, e)
                    target.beginScratch(sx, sy)
                } else {
                    val (sx, sy) = toLocalInScratch(target, e)
                    target.scratchTo(sx, sy)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeScratchView?.endScratch()
                activeScratchView = null
                rv.requestDisallowInterceptTouchEvent(false)
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // no-op
    }

    /** 找到手指正下方的 item 对应的 ScratchCardView（若没有则返回 null） */
    private fun findScratchUnder(e: MotionEvent): ScratchCardView? {
        val child: View = recyclerView.findChildViewUnder(e.x, e.y) ?: return null
        return child.findViewById(R.id.scratch)
    }

    /** 把屏幕坐标转换到 scratchView 的本地坐标系 */
    private fun toLocalInScratch(scratchView: View, e: MotionEvent): Pair<Float, Float> {
        val loc = IntArray(2)
        scratchView.getLocationOnScreen(loc)
        val localX = e.rawX - loc[0]
        val localY = e.rawY - loc[1]
        return localX to localY
    }
}
