package com.example.charge.utils

import kotlin.random.Random

/**
 * 随机数工具类
 */
object RandomUtils {
    
    /**
     * 根据概率判断是否执行某个动作
     * @param probability 概率值，范围0f-1f
     * @return true: 概率命中，false: 概率未命中
     */
    fun isProbabilityHit(probability: Float): Boolean {
        // 边界检查
        val validProbability = when {
            probability < 0f -> 0f
            probability > 1f -> 1f
            else -> probability
        }
        
        // 生成0-1之间的随机数并比较
        return Random.nextFloat() < validProbability
    }
    
    /**
     * 生成指定范围内的随机整数
     */
    fun randomInt(min: Int, max: Int): Int {
        return Random.nextInt(min, max + 1)
    }
    
    /**
     * 生成指定范围内的随机浮点数
     */
    fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }
}

/**
 * 使用示例：
 * 
 * // 示例1：有30%的概率执行某个操作
 * if (RandomUtils.isProbabilityHit(0.3f)) {
 *     // 执行概率为30%的操作
 * }
 * 
 * // 示例2：100%概率（必定执行）
 * if (RandomUtils.isProbabilityHit(1.0f)) {
 *     // 必定执行的操作
 * }
 * 
 * // 示例3：0%概率（必定不执行）
 * if (RandomUtils.isProbabilityHit(0.0f)) {
 *     // 不会执行的操作
 * }
 * 
 * // 示例4：边界值自动修正
 * if (RandomUtils.isProbabilityHit(1.5f)) { // 会被修正为1.0f
 *     // 必定执行的操作
 * }
 */