package com.carefree.steplib.core

import android.content.Context

/**
 * 基于加速度传感器的计步检测器接口
 */
interface StepDetectorInterface : StepCounterInterface {
    
    /**
     * 设置计步灵敏度（范围：1-10，默认：5）
     */
    fun setSensitivity(sensitivity: Int)
}