package com.github.shadowsocks.utils

import kotlinx.coroutines.*

class CountdownManager(private val initialTime: Long) {

    private var currentTime = initialTime
    private var timeLeft = currentTime
    private var mUsedTime = 0L
    private var job: Job? = null

    var onTickAction: (Long) -> Unit = {}

    var onFinishAction: () -> Unit = {}

    fun startCountdown() {
        timeLeft = currentTime
        job = GlobalScope.launch(Dispatchers.Main) {
            while (isActive && timeLeft > 0) {
                onTickAction(timeLeft)
                delay(1000)
                timeLeft -= 1000
                mUsedTime += 1000
            }
            onFinishAction()
        }
    }

    fun addTime(timeToAdd: Long) {
        currentTime += timeToAdd
        timeLeft += timeToAdd
    }

    fun cancel() {
        job?.cancel()
        mUsedTime = 0L
    }

    fun reset() {
        currentTime = initialTime
        mUsedTime = 0L
    }

    fun getUsedTime(): Long {
        return mUsedTime
    }
}