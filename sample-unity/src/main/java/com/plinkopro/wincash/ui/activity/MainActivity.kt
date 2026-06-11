package com.plinkopro.wincash.ui.activity

import android.os.Bundle
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.CoroutineHelper
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity
import kotlinx.coroutines.launch

/**
 * 包名必须和unity里面的一致
 * com.plinkopro.wincash.ui.activity.MainActivity
 */
class MainActivity: UnityPlayerActivity() {

    companion object {
        var currentActivity: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mUnityPlayer == null) {
            mUnityPlayer = UnityPlayer(this)
            UnityPlayer.currentActivity = this
            currentActivity = this
        }

        // 请求焦点以确保Unity能够接收输入
        mUnityPlayer?.requestFocus()
        // 确保Unity知道窗口获得焦点
        mUnityPlayer?.windowFocusChanged(true)

        //打印日志：Unity                   com.allever.business.lib.project     I  [AndroidToUnity] onRewarded， 调用成功
        UnityPlayer.UnitySendMessage("Android", "onRewarded", "")
    }

    fun showRewardAd() {
        CoroutineHelper.MAIN.launch {
            toast("showRewardAd")
        }
    }

    fun showInterAd() {
        CoroutineHelper.MAIN.launch {
            toast("showInterAd")
        }
    }

    //unity中方法名不能以大写开头
    fun ShowAd() {
        CoroutineHelper.MAIN.launch {
            toast("showAd")
        }
    }

    override fun onStart() {
        super.onStart()
        mUnityPlayer?.windowFocusChanged(true)
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
        mUnityPlayer?.windowFocusChanged(false)
    }

    // 活动销毁时的回调
    override fun onDestroy() {
        super.onDestroy()
//        mUnityPlayer?.quit() // 退出 UnityPlayer
        mUnityPlayer = null // 释放 UnityPlayer 实例
    }

}