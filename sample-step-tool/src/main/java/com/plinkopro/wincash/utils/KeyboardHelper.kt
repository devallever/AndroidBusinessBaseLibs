package com.plinkopro.wincash.utils

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager

/**
 * 键盘监听工具类
 * 提供软键盘显示和隐藏的监听功能，支持任意Activity使用
 */
class KeyboardHelper private constructor(
    private val activity: Activity,
    private val onKeyboardVisibilityChanged: (Boolean) -> Unit
) {
    
    private var isKeyboardVisible = false
    private var keyboardLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    
    companion object {
        // 键盘高度阈值，超过屏幕高度的1/4则认为键盘弹出
        private const val KEYBOARD_HEIGHT_RATIO = 0.25
        
        /**
         * 创建键盘监听助手实例
         * @param activity 要监听键盘的Activity
         * @param onKeyboardVisibilityChanged 键盘状态变化回调
         * @return KeyboardHelper 实例
         */
        fun create(
            activity: Activity,
            onKeyboardVisibilityChanged: (Boolean) -> Unit
        ): KeyboardHelper {
            return KeyboardHelper(activity, onKeyboardVisibilityChanged).also {
                it.setupKeyboardListener()
            }
        }
    }
    
    /**
     * 设置键盘监听器
     */
    private fun setupKeyboardListener() {
        val rootView = activity.findViewById<View>(android.R.id.content)
        
        // 等待视图完全渲染后再设置监听器
        rootView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // 移除预绘制监听器
                rootView.viewTreeObserver.removeOnPreDrawListener(this)
                
                // 现在设置键盘监听器
                setupKeyboardLayoutListener(rootView)
                return true
            }
        })
    }
    
    /**
     * 实际的键盘布局监听器设置
     */
    private fun setupKeyboardLayoutListener(rootView: View) {
        // 使用屏幕高度作为基准
        val screenHeight = rootView.resources.displayMetrics.heightPixels
        // 键盘最小高度阈值
        val keyboardThreshold = (screenHeight * KEYBOARD_HEIGHT_RATIO).toInt()

        keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener { 
            val r = Rect()
            // 获取当前窗口可见区域
            activity.window.decorView.getWindowVisibleDisplayFrame(r)
            
            // 计算屏幕高度减去可见区域高度
            val heightDiff = screenHeight - (r.bottom - r.top)
            
            // 判断键盘是否可见
            val newKeyboardVisible = heightDiff > keyboardThreshold
            
            if (isKeyboardVisible != newKeyboardVisible) {
                isKeyboardVisible = newKeyboardVisible
                onKeyboardVisibilityChanged(isKeyboardVisible)
            }
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
    }
    
    /**
     * 移除键盘监听器，避免内存泄漏
     */
    fun removeKeyboardListener() {
        val rootView = activity.findViewById<View>(android.R.id.content)
        keyboardLayoutListener?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(it)
            } else {
                @Suppress("DEPRECATION")
                rootView.viewTreeObserver.removeGlobalOnLayoutListener(it)
            }
            keyboardLayoutListener = null
        }
    }
    
    /**
     * 获取当前键盘是否可见
     */
    fun isKeyboardVisible(): Boolean {
        return isKeyboardVisible
    }
    
    /**
     * 隐藏软键盘
     */
    fun hideKeyboard() {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        // 查找当前焦点的视图
        val view = activity.currentFocus ?: View(activity)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    
    /**
     * 显示软键盘
     * @param view 要获取焦点的视图
     */
    fun showKeyboard(view: View) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}