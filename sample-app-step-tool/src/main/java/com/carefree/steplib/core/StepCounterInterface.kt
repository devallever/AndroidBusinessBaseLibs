package com.carefree.steplib.core

import android.content.Context

/**
 * 计步器核心接口
 */
interface StepCounterInterface {
    /**
     * 获取当前步数
     */
    val currentStep: Int
    
    /**
     * 设置步数变化监听器
     */
    fun setStepChangeListener(listener: StepChangeListener?)
    
    /**
     * 重置步数
     */
    fun resetSteps()
    
    /**
     * 停止计步
     */
    fun stop()
    
    /**
     * 开始计步
     */
    fun start(context: Context)
}