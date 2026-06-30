package com.carefree.steplib.step

/**
 * Created by jiahongfei on 2017/6/30.
 */
interface StepCounterListener {
    /**
     * 用于显示步数
     * @param step
     */
    fun stepChange(step: Int)

    /**
     * 步数清零监听，由于跨越0点需要重新计步
     */
    fun stepClean()
}
