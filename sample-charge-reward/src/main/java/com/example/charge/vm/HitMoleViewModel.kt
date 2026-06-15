package com.example.charge.vm

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.allever.android.lib.core.app.App
import com.example.charge.init.FpManger
import com.example.charge.utils.LogUtil
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil

class HitMoleViewModel : ViewModel() {

    var residueInfiniteTime = MutableLiveData<Long>()  //剩余的无限制时间
    var gameCount = MutableLiveData<Int>()  //剩余的打地鼠次数
    var countDownTimer = MutableLiveData<Long>() //获取次数倒计时
    var isInfiniteTime = MutableLiveData<Boolean>() //是否是无限制时间

    val addGameCountTime = FpManger.chargeConfig.addGameSizeTime * 1000L  // 添加次数的倒计时时间
    val onceInfiniteTime = 15 * 60 * 1000L // 一次无限制的时间

    var startCountDownTimer = true
    var startInfiniteCountDownTimer = true

    init {
        updateResidueInfiniteTime()
        updateGameCount()

        residueInfiniteTime.observeForever {
            SpUtil.put(SpKey.HIT_MOLE_GAME_COUNT_INFINITE_TIME, it)
            isInfiniteTime.value  = residueInfiniteTime.value!! > 0
        }
        gameCount.observeForever {
            SpUtil.put(SpKey.HIT_MOLE_GAME_COUNT, it)
            if (it < 3 && startCountDownTimer) {
                startCountDownTimer = false
                if (App.DEBUG) {
                    LogUtil.hitMole("打地鼠次数不足三次，当前次数：$it 开始获取倒计时，剩余获取时间：${countDownTimer.value}")
                }
                getCountDownTimer(
                    countDownTimer.value!!,
                    1000L,
                    {
                        countDownTimer.value = it
                    },
                    {
                        startCountDownTimer = true
                        countDownTimer.value = addGameCountTime
                        gameCount.value = gameCount.value?.plus(1)
                        if (App.DEBUG) {
                            LogUtil.hitMole("倒计时结束获取打地鼠次数，当前次数${gameCount.value}")
                        }
                    }).start()
            }
        }
        countDownTimer.observeForever {
            SpUtil.put(SpKey.HIT_MOLE_GAME_ADD_COUNT_TIME, it)
        }

        isInfiniteTime.observeForever {
            if (App.DEBUG) {
                LogUtil.hitMole("打地鼠无限次数？$it")
            }
            if (it && startInfiniteCountDownTimer) {
                startInfiniteCountDownTimer = false
                getCountDownTimer(
                    residueInfiniteTime.value!!,
                    1000L,
                    {
                        residueInfiniteTime.value = it
                        if (App.DEBUG) {
                            LogUtil.hitMole("打地鼠无限次数倒计时，当前剩余无限制时间：${residueInfiniteTime.value}")
                        }
                    },
                    {
                        isInfiniteTime.value = false
                        startInfiniteCountDownTimer = true
                        if (App.DEBUG) {
                            LogUtil.hitMole("打地鼠无限次数时间耗尽,isInfiniteTime: ${isInfiniteTime.value},residueInfiniteTime: ${residueInfiniteTime.value}")
                        }
                    }).start()
            }
        }
    }

    fun updateResidueInfiniteTime() {
        val residueTime = SpUtil.get(SpKey.HIT_MOLE_GAME_COUNT_INFINITE_TIME, 0L)
        val lastAppQuitTime = SpUtil.get(SpKey.APP_QUIT_TIME, 0L)
        val quitTime = System.currentTimeMillis() - lastAppQuitTime
        if (App.DEBUG) {
            LogUtil.hitMole("打地鼠无限时间更新，离线时间：$quitTime 剩余无限时间：$residueTime")
        }
        residueInfiniteTime.value = if (quitTime >= residueTime) {
            if (App.DEBUG) {
                LogUtil.hitMole("离线时间大于剩余无限时间，无限时间清零")
            }
            0L
        } else {
            if (App.DEBUG) {
                LogUtil.hitMole("离线时间小于剩余无限时间，剩余无限时间：${residueTime - quitTime}")
            }
            residueTime - quitTime
        }
    }

    fun updateGameCount() {
        var count = SpUtil.get(SpKey.HIT_MOLE_GAME_COUNT, 3)
        val lastAppQuitTime = SpUtil.get(SpKey.APP_QUIT_TIME, 0L)
        var quitTime = System.currentTimeMillis() - lastAppQuitTime
        var residueAddTime = SpUtil.get(SpKey.HIT_MOLE_GAME_ADD_COUNT_TIME, addGameCountTime)
        if (App.DEBUG) {
            LogUtil.hitMole("打地鼠次数更新，离线时间：$quitTime 游戏次数：$count 剩余获取时间：${residueAddTime}" )
        }
        if (count < 3) {
            if (App.DEBUG) {
                LogUtil.hitMole("打地鼠次数小于3，计算离线补偿")
            }
            if (quitTime >= residueAddTime) {
                count++
                quitTime -= residueAddTime
                if (App.DEBUG) {
                    LogUtil.hitMole("离线时间大于剩余获取时间，打地鼠次数+1：$count 剩余离线时间 = 离线时间-剩余获取时间：${quitTime}")
                }
                while (quitTime > 0 && count < 3) {
                    if (quitTime >= addGameCountTime) {
                        count++
                        quitTime -= addGameCountTime
                        residueAddTime = addGameCountTime
                        if (App.DEBUG) {
                            LogUtil.hitMole("可用离线时间大于一次获取时间 并且 打地鼠次数小于3，打地鼠次数+1：$count  获取一次机会所需时间：${addGameCountTime}  剩余可用离线时间 = 离线时间-获取一次机会所需时间：${quitTime}")
                        }
                    } else {
                        residueAddTime = addGameCountTime - quitTime
                        quitTime = 0
                        if (App.DEBUG) {
                            LogUtil.hitMole("可用离线时间小于一次获取时间 下一次获取时间 = 获取一次机会所需时间（${addGameCountTime}）- 可用离线时间（${quitTime}）：${residueAddTime}")
                        }
                    }
                }

            } else {
                residueAddTime = residueAddTime - quitTime
                if (App.DEBUG) {
                    LogUtil.hitMole("离线时间小于剩余获取时间，剩余获取时间 = 剩余获取时间（${residueAddTime}）- 离线时间（${quitTime}）：${residueAddTime}")
                }
            }
        }
        gameCount.value = count
        countDownTimer.value = residueAddTime
    }

    fun getCountDownTimer(
        millisInFuture: Long,
        countDownInterval: Long,
        tickCallBack: (Long) -> Unit = {},
        finishCallBack: () -> Unit = {}
    ): CountDownTimer {
        return object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                tickCallBack.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                finishCallBack.invoke()
            }
        }
    }

}