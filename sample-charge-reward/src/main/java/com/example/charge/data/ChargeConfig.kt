package com.example.charge.data

import androidx.annotation.Keep

@Keep
data class ChargeConfig(
    val gameEndInterAdProbability: Float = 0.5f,//游戏结束触发插屏概率
    /***
     *     第一套：插屏CD：默认90秒（后台可配置）90秒内触发激励视频，CD刷新，在游戏内，需要等到游戏时间=0（即结算前），如果在游戏里看了激励视频，也可以刷新CD。
     *     第二套：插屏CD：默认90秒（后台可配置）90秒内触发激励视频，CD刷新，每次游戏结束概率弹插屏，后台可以配置概率，触发了插屏后，CD刷新。如果游戏中，CD到，游戏结束必弹。
     */
    val interLogic: Int = 1, //插屏逻辑
    val interAdTime: Int = 90, //插屏cd时间
    val userAb : String = "",
    val addGameSizeTime : Int = 30, //添加一次游戏次数的时间
    val newUserMinAware : Int = 20, //新用户最小奖励
    val newUserMaxAware : Int = 40,
    val withdraw: MutableList<Withdraw> = mutableListOf<Withdraw>()
) {

}