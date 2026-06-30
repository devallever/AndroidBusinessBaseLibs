package com.carefree.steplib.core

/**
 * 计步变化监听器接口
 */
interface StepChangeListener {
    /**
     * 当步数发生变化时调用
     * @param step 当前步数
     */
    fun onStepChanged(step: Int)

    /**
     * 当步数清零时调用
     */
    fun onStepClean()
}