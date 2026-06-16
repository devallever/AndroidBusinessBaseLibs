package app.flash.tunnel.vpn.lib.common.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MultiStepProgress {
    //total duration
    var duration = 0L
    private var isLoading = false
        private set

    //level speed
    val listSpeed: MutableList<Speed> = mutableListOf()
    private var finishFlag = false
    private var isCancel = false
    private fun getStep(progress: Int): Long {
        if (finishFlag) {
            return 10L
        }
        var totalPercent = 0.0
        listSpeed.mapIndexed { index, speed ->
            totalPercent += speed.progressPercent
//                log("progress = $progress, totalPercent = ${totalPercent * 100}")
            if (progress <= totalPercent * 100) {
                return speed.step
            }
        }
        return 10L
    }

    fun reset() {
        isLoading = false
        finishFlag = false
        isCancel = false
    }

    fun finish() {
        finishFlag = true
    }

    private var progressInvoke: (progress: Int) -> Unit = {}

    suspend fun start(block: (progress: Int) -> Unit) {
        if (isLoading) {
            return
        }
        progressInvoke = block
        finishFlag = false
        isCancel = false
        withContext(Dispatchers.IO) {
            var progress = 0
            var usedTime = 0F
            isLoading = true
            while (isActive && progress < 100 && !isCancel) {
                val step = getStep(progress)
//                log("step = ${step}")
                delay(step)
                usedTime += step
                progress += 1
//                log("usedTime = ${usedTime / 1000f}")

                if (isCancel) {
                    return@withContext
                }

                launch(Dispatchers.Main) {
                    block.invoke(progress)
                }
            }
            isLoading = false
        }
    }

    fun cancel() {
        isCancel = true
    }

    fun setProgress(value: Float) {
        progressInvoke.invoke(value.toInt())
    }

    fun updateDuration(value: Long) {
        duration = value
    }

    data class Speed(
        val total: Long = 0L,
        var progressPercent: Double = 0.0,
        var durationPercent: Double = 0.0
    ) {

        var step = 10L

        init {
            step = (durationPercent * total / (progressPercent * 100)).toInt().toLong()
            log("step = $step")
        }
    }
}