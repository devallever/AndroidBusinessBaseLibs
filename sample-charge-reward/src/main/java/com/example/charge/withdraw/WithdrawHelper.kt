package com.example.charge.withdraw

import app.allever.android.lib.core.app.App
import com.example.charge.constant.WithdrawLevel
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.data.ChargeConfig
import com.example.charge.event.WaitingPlayerUpdateEvent
import com.example.charge.init.FpManger
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil
import com.example.charge.utils.WaitingPlayerStrategy
import com.example.charge.utils.log
import org.greenrobot.eventbus.EventBus

object WithdrawHelper {

    var chargeConfig = FpManger.chargeConfig
    val playerStrategy by lazy {
        WaitingPlayerStrategy()
    }

    /**
     * 后台配置， 先写死
     */
    fun getWithdrawLevelValue(level: Int): Int {
        return when(level) {
            WithdrawLevel.LEVEL_1 -> {
                return if (chargeConfig.withdraw.isNotEmpty()) {
                    chargeConfig.withdraw[0].limit
                } else {
                    299
                }
            }
            WithdrawLevel.LEVEL_2 -> {
                return if (chargeConfig.withdraw.size > 1) {
                    chargeConfig.withdraw[1].limit
                } else {
                    699
                }
            }
            WithdrawLevel.LEVEL_3 -> {
                return if (chargeConfig.withdraw.size > 2) {
                    chargeConfig.withdraw[2].limit
                } else {
                    999
                }
            }
            else -> {
                299
            }
        }
    }

    fun updateWithdrawConfig() {
        val json = FpManger.getWithdrawSetting()
    }

    /**
     * 是否显示排队
     */
    fun showWaitingPlayer(): Boolean {
        return CurrencyUtils.getCurrencyNum(CurrencyType.GREEN) >= getShowLineUpNum(1)
    }

    fun showRedeem(level: Int): Boolean {
        when (level) {
            WithdrawLevel.LEVEL_1 -> {
                if (chargeConfig.withdraw.isNotEmpty()) {
                    return CurrencyUtils.getCurrencyNum(CurrencyType.GREEN) >= getWithdrawLevelValue(level)
                }
            }
            WithdrawLevel.LEVEL_2 -> {
                if (chargeConfig.withdraw.size > 1) {
                    return CurrencyUtils.getCurrencyNum(CurrencyType.GREEN) >= getWithdrawLevelValue(level)
                }
            }
            WithdrawLevel.LEVEL_3 -> {
                if (chargeConfig.withdraw.size > 2) {
                    return CurrencyUtils.getCurrencyNum(CurrencyType.GREEN) >= getWithdrawLevelValue(level)
                }
            }
        }
        return false
    }

    fun getShowLineUpNum(level: Int) : Int{
        return when(level) {
            WithdrawLevel.LEVEL_1 -> {
                return if (chargeConfig.withdraw.isNotEmpty()) {
                    chargeConfig.withdraw[0].showLineUpNum
                } else {
                    299
                }
            }
            WithdrawLevel.LEVEL_2 -> {
                return if (chargeConfig.withdraw.size > 1) {
                    chargeConfig.withdraw[1].showLineUpNum
                } else {
                    500
                }
            }
            WithdrawLevel.LEVEL_3 -> {
                return if (chargeConfig.withdraw.size > 2) {
                    chargeConfig.withdraw[2].showLineUpNum
                } else {
                    800
                }
            }
            else -> {
                0
            }
        }
    }


    private var level1Player = SpUtil.get(SpKey.WAITING_PLAYER_COUNT_1, playerStrategy.getDefaultLevel1Players())
    private var level2Player = SpUtil.get(SpKey.WAITING_PLAYER_COUNT_2, 0)
    private var level3Player =  SpUtil.get(SpKey.WAITING_PLAYER_COUNT_3, 0)


    fun getWaitingPlayerCount(level: Int): Int {
        return when(level) {
            WithdrawLevel.LEVEL_1 -> {
                return level1Player
            }
            WithdrawLevel.LEVEL_2 -> {
                return level2Player
            }
            WithdrawLevel.LEVEL_3 -> {
                return level3Player
            }
            else -> {
                0
            }
        }
    }
    fun updateWaitingPlayer() {
        if (!showWaitingPlayer()) {
            if (App.DEBUG) {
                log("没有到达展示排队条件")
            }
            return
        }
        level1Player = playerStrategy.updateLevel1Strategy(level1Player)
        SpUtil.put(SpKey.WAITING_PLAYER_COUNT_1, level1Player)
        level2Player = playerStrategy.updateLevel2Strategy(level2Player)
        SpUtil.put(SpKey.WAITING_PLAYER_COUNT_2, level2Player)
        level3Player = playerStrategy.updateLevel3Strategy(level3Player)
        SpUtil.put(SpKey.WAITING_PLAYER_COUNT_3, level3Player)

        EventBus.getDefault().post(WaitingPlayerUpdateEvent())
    }

    fun debugUpdateWaitingPlayer(level: Int, count: Int) {
        when(level) {
            WithdrawLevel.LEVEL_1 -> {
                level1Player = count
                SpUtil.put(SpKey.WAITING_PLAYER_COUNT_1, level1Player)
            }
            WithdrawLevel.LEVEL_2 -> {
                level2Player = count
                SpUtil.put(SpKey.WAITING_PLAYER_COUNT_2, level2Player)
            }
            WithdrawLevel.LEVEL_3 -> {
                level3Player = count
                SpUtil.put(SpKey.WAITING_PLAYER_COUNT_3, level3Player)
            }
        }

        if (App.DEBUG) {
            log("人数加减策略", "手动更新 ${level} 档人数为 $count")
        }
        EventBus.getDefault().post(WaitingPlayerUpdateEvent())
    }
}