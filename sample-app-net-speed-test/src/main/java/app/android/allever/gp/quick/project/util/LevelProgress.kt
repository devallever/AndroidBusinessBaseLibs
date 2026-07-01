package app.android.allever.gp.quick.project.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import app.allever.android.lib.core.helper.CoroutineHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: 三段式快-慢-超慢-动画进度
 *@date: 2023/11/7
 */
class LevelProgress(lifecycleOwner: LifecycleOwner) : LifecycleEventObserver {
    companion object {
        private const val TOTAL_TIME = 10 * 1000L
        private const val FIRST_STEP_TIME = 5 * 1000L
        private const val SECOND_STEP_TIME = 3 * 1000L
        private const val FIRST_DELAY_STEP = FIRST_STEP_TIME / 80L
        private const val SECOND_DELAY_STEP = (SECOND_STEP_TIME - FIRST_STEP_TIME) / 50
        private const val FINAL_DELAY_STEP = (TOTAL_TIME - SECOND_STEP_TIME) / 30
    }

    private val _progressLiveData = MutableStateFlow(0)
    val progressLiveData = _progressLiveData.asStateFlow()


    private var loadingStartTime = 0L
    private var delayStep = FIRST_DELAY_STEP

    private var isOnForGround = true

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun loadProgressAnim() {
        loadingStartTime = System.currentTimeMillis()
        CoroutineHelper.MAIN.launch {
            var progress = _progressLiveData.value ?: 0
            while (isActive && progress < 100 && !mPause) {
                delay(delayStep)
                progress = _progressLiveData.value ?: 0
                if (progress < 100) {
                    _progressLiveData.value = (progress + 1)
//                    log("progress = $progress")
                }
                if (System.currentTimeMillis() - loadingStartTime > FIRST_STEP_TIME && delayStep == FIRST_DELAY_STEP) {
                    delayStep = SECOND_DELAY_STEP
                } else if (System.currentTimeMillis() - loadingStartTime > SECOND_STEP_TIME && delayStep == SECOND_DELAY_STEP) {
                    delayStep = FINAL_DELAY_STEP
                }
            }
        }
    }

    fun finishLoading() {
        val current = System.currentTimeMillis()
        val remainTime = loadingStartTime + FIRST_STEP_TIME - current
        if (remainTime <= 0) {
            _progressLiveData.value = (100)
        } else {
            val remainProgress = 100 - _progressLiveData.value!!
            delayStep = remainTime / remainProgress
        }
    }

    fun isFinish() = _progressLiveData.value >= 100

    private var mPause = false
    fun pause() {
        mPause
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//        log("LevelProgress: onStateChanged: ${event.name}")
        isOnForGround = event != Lifecycle.Event.ON_STOP && event != Lifecycle.Event.ON_DESTROY
//        log("LevelProgress: onStateChanged: isOnForGround = $isOnForGround")
    }
}