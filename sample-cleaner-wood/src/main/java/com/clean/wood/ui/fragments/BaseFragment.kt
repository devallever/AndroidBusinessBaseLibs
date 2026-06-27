package com.clean.wood.ui.fragments

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.clean.wood.MainActivity
import com.clean.wood.WoodApp
import com.clean.wood.data.AdManager
import com.clean.wood.utils.Constant
import com.clean.wood.utils.PollingTask
import com.clean.wood.utils.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseFragment : Fragment() {
    abstract fun stackKey(): String
    protected var adWaitingTask: PollingTask? = null
    private var initShow = true
    private var isHidden = true

    fun pushFragment(fragment: BaseFragment) {
        val host = activity
        if (host is MainActivity) {
            host.push(fragment)
        }
    }

    fun pop() {
        val host = activity
        if (host is MainActivity) {
            host.pop()
        }
    }

    open fun backPressedEnable(): Boolean {
        return false
    }

    fun refreshBackPressedState() {
        val host = activity
        if (host is MainActivity) {
            host.refreshBackPressedState()
        }
    }

    open fun onBackPressed() {}

    open fun onShow() {}
    open fun onHide() {}

    fun changeHideState(hidden: Boolean) {
        if (hidden != isHidden) {
            isHidden = hidden
            if (hidden) {
                onHide()
            } else {
                onShow()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (initShow && !WoodApp.alreadyInBackground) {
            initShow = false
            val host = activity
            if (host is MainActivity) {
                if (host.getTopFragmentKey() == stackKey()) {
                    changeHideState(false)
                }
            } else {
                changeHideState(false)
            }
        }
    }

    protected fun handleClickBack() {
        lifecycleScope.launch {
            AdManager.ins.showInterAd(Constant.AdPosition.ExitInter)
            pop()
//            toast("showExitAd")
        }
    }

    protected fun waitingAd(
        check: () -> Boolean,
        next: () -> Unit,
        timeOut: () -> Unit,
        scene: String = this::class.simpleName ?: "default"
    ) {
        lifecycleScope.launch {
            delay(Constant.ANIMATION_MIN_DURATION)
            val interval = 500L
            val retryCount =
                (Constant.ANIMATION_MAX_DURATION - Constant.ANIMATION_MIN_DURATION) / interval
            adWaitingTask = PollingTask(
                viewLifecycleOwner,
                interval,
                mTaskName = scene,
                mMaxRetry = retryCount.toInt(),
                mCondition = {
                    check()
                },
                mExecute = {
                    next.invoke()
                },
                mOnFail = {
                    timeOut.invoke()
                })
            adWaitingTask?.start()
        }
    }

    protected fun waitingAd2(
        actionStartTime: Long,
        check: () -> Boolean,
        next: () -> Unit,
        timeOut: () -> Unit,
        scene: String = this::class.simpleName ?: "default"
    ) {
        lifecycleScope.launch {
            val interval = 500L

            val endTime = System.currentTimeMillis()
            val usedTime = endTime - actionStartTime
            val delayTime = if (usedTime < Constant.ANIMATION_MIN_DURATION) {
                Constant.ANIMATION_MIN_DURATION - usedTime
            } else {
                0
            }
            val retryCount = if (usedTime < Constant.ANIMATION_MAX_DURATION) {
                (Constant.ANIMATION_MAX_DURATION - usedTime) / interval
            } else {
                1
            }

            delay(delayTime)

            PollingTask(
                viewLifecycleOwner,
                interval,
                mTaskName = scene,
                mMaxRetry = retryCount.toInt(),
                mCondition = {
                    check()
                },
                mExecute = {
                    next.invoke()
                },
                mOnFail = {
                    timeOut.invoke()
                }).start()
        }
    }
}