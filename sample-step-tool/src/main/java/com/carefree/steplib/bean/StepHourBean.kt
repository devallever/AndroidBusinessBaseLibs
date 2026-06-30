package com.carefree.steplib.bean

import kotlinx.serialization.Serializable

/**
 *classDes Walk
 *@author 稻谷
 *create date 2023/11/7
 */
@Serializable
data class StepHourBean(
    var hour: Int,
    var step:Int
)
