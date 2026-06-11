package app.allever.android.sample.unity

import android.content.res.Configuration
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.unity.databinding.ActivityUnityContainerBinding
import com.unity3d.player.UnityPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnityContainerActivity: BaseActivity<ActivityUnityContainerBinding, BaseViewModel>() {

    companion object {
        var currentActivity: UnityContainerActivity? = null
    }

    private var mUnityPlayer: UnityPlayer? = null

    override fun inflateChildBinding(): ActivityUnityContainerBinding = ActivityUnityContainerBinding.inflate(layoutInflater)

    override fun init() {
        initUnity()
        //public static void UnitySendMessage(String var0, String var1, String var2)
        UnityPlayer.UnitySendMessage("Android", "onRewarded", "")
    }

    private fun initUnity() {
        currentActivity = this
        // 初始化UnityPlayer实例
        if (mUnityPlayer == null) {
            mUnityPlayer = UnityHelper.unityPlayer ?: UnityPlayer(this)
            UnityPlayer.currentActivity = this
        }
        // 获取Unity视图容器并添加UnityPlayer视图
        binding.unityViewContainer.addView(
            mUnityPlayer?.view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        // 请求焦点以确保Unity能够接收输入
        mUnityPlayer?.requestFocus()
        // 确保Unity知道窗口获得焦点
        mUnityPlayer?.windowFocusChanged(true)
    }

    override fun onStart() {
        super.onStart()
        mUnityPlayer?.windowFocusChanged(true) // UnityPlayer 可能需要这个
    }

    // 活动暂停时的回调
    override fun onPause() {
        super.onPause()
        mUnityPlayer?.pause()
    }

    // 活动恢复时的回调
    override fun onResume() {
        super.onResume()
        mUnityPlayer?.resume()
    }

    // 活动停止时的回调
    override fun onStop() {
        super.onStop()
        mUnityPlayer?.windowFocusChanged(false) // UnityPlayer 可能需要这个
    }

    // 活动销毁时的回调
    override fun onDestroy() {
        super.onDestroy()
        mUnityPlayer?.quit() // 退出 UnityPlayer
        mUnityPlayer = null // 释放 UnityPlayer 实例
        UnityHelper.destroyUnityPlayer()
    }

    // 处理配置变化（可选，但推荐）
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        mUnityPlayer?.configurationChanged(newConfig)
//    }

    // 处理窗口焦点变化
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mUnityPlayer?.windowFocusChanged(hasFocus)
    }

    // 处理按键按下事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mUnityPlayer?.injectEvent(event) ?: super.onKeyDown(keyCode, event)
    }

    // 处理按键抬起事件
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return mUnityPlayer?.injectEvent(event) ?: super.onKeyUp(keyCode, event)
    }

    // 处理触摸事件
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mUnityPlayer?.injectEvent(event) ?: super.onTouchEvent(event)
    }

    // 处理通用运动事件
    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        return mUnityPlayer?.injectEvent(event) ?: super.onGenericMotionEvent(event)
    }

    fun showAd() {
        lifecycleScope.launch(Dispatchers.Main) {
            toast("showAd")
        }
    }
}