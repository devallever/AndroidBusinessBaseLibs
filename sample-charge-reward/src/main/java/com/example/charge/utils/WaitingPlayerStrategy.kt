package com.example.charge.utils
import app.allever.android.lib.core.app.App
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.withdraw.WithdrawHelper
import kotlin.random.Random

class WaitingPlayerStrategy {

    private val TAG = "人数加减策略"

    var minPlayerCount: Int = 15
    var maxPlayerCount: Int = 100
    var defaultMinPlayers: Int = 500
    var defaultMaxPlayers: Int = 1000

    /**
     * $299档位策略
     */
    fun updateLevel1Strategy(currentPlayers: Int = getDefaultLevel1Players()): Int {
        var newPlayers = currentPlayers
        if (App.DEBUG) {
            log(TAG, "更新1档人数========================================================================")
            log(TAG, "更新1档人数->当前人数：$currentPlayers")
        }

        // 当总人数小于15时的特殊逻辑
        if (newPlayers < minPlayerCount) {
            // 只增加10-20人
            newPlayers += Random.nextInt(10, 21)
            if (App.DEBUG) {
                log(TAG, "更新1档人数->特殊逻辑：总人数小于15，增加10-20人 -> 更新人数${newPlayers}")
            }
        } else {
            // 50%概率减少1-10人，50%概率增加1-5人
            if (Random.nextBoolean()) {
                // 减少1-10人
                val decrease = Random.nextInt(1, 11)
                newPlayers = maxOf(0, newPlayers - decrease)
                if (App.DEBUG) {
                    log(TAG, "更新1档人数-> 50%概率减少10-1人：减少了$decrease")
                }
            } else {
                // 增加1-5人
                val increase = Random.nextInt(1, 6)
                newPlayers += increase
                if (App.DEBUG) {
                    log(TAG, "更新1档人数-> 50%概率增加5-1人：增加了$increase")
                }
            }

            if (App.DEBUG) {
                log(TAG, "更新1档人数->当前人数：$currentPlayers -> 新人数：$newPlayers")
            }
        }

        return newPlayers
    }

    /**
     * $699档位策略
     */
    fun updateLevel2Strategy(currentPlayers: Int): Int {
        if (App.DEBUG) {
            log(TAG, "更新2档人数========================================================================")
            log(TAG, "更新2档人数->当前人数：$currentPlayers")
        }
        // 当钱包金额小于$500时，显示等候人数为0
        val threadHold = WithdrawHelper.getShowLineUpNum(2)
        val walletAmount = CurrencyUtils.getCurrencyNum(CurrencyType.GREEN)
        if (walletAmount < threadHold) {
            if (App.DEBUG) {
                log(TAG, "更新2档人数->当前钱包金额${walletAmount}小于${threadHold}，显示等候人数为0")
            }
            return 0
        }

        var newPlayers = currentPlayers

        // 当总人数小于15时的特殊逻辑
        if (newPlayers < minPlayerCount) {
            // 增加10-20人
            newPlayers += Random.nextInt(10, 21)
            if (App.DEBUG) {
                log(TAG, "更新2档人数->特殊逻辑：总人数小于15，增加10-20人 -> 更新人数${newPlayers}")
            }
            return newPlayers
        } else {
            val arriveRandom = SpUtil.get(SpKey.ARRIVE_LEVEL2_RANDOM, false)
            if (newPlayers < maxPlayerCount && !arriveRandom) {
                // 每分钟增加1-10人，到达100人后开始下面逻辑
                val increase = Random.nextInt(1, 11)
                newPlayers += increase
                if (App.DEBUG) {
                    log(TAG, "更新2档人数->未到达100人，每分钟增加1-10人：增加了${increase}, 更新人数${newPlayers}")
                }
                return newPlayers
            } else {
                //到达过100人，永久随机
                SpUtil.put(SpKey.ARRIVE_LEVEL2_RANDOM, true)
                // 50%概率减少1-5人，50%概率增加1-3人
                if (Random.nextBoolean()) {
                    // 减少1-5人
                    val decrease = Random.nextInt(1, 6)
                    newPlayers = maxOf(0, newPlayers - decrease)
                    if (App.DEBUG) {
                        log(TAG, "更新2档人数-> 50%概率减少5-1人：减少了$decrease")
                    }
                } else {
                    // 增加1-3人
                    val increase = Random.nextInt(1, 4)
                    newPlayers += increase
                    if (App.DEBUG) {
                        log(TAG, "更新2档人数-> 50%概率增加3-1人：增加了$increase")
                    }
                }
                if (App.DEBUG) {
                    log(TAG, "更新2档人数->当前人数：$currentPlayers -> 更新人数：$newPlayers")
                }
                return newPlayers
            }
        }

    }

    /**
     * $999档位策略
     */
    fun updateLevel3Strategy(currentPlayers: Int): Int {
        if (App.DEBUG) {
            log(TAG, "更新3档人数========================================================================")
            log(TAG, "更新3档人数->当前人数：$currentPlayers")
        }
        // 当钱包金额小于$800时，显示等候人数为0
        val threadHold = WithdrawHelper.getShowLineUpNum(3)
        val walletAmount = CurrencyUtils.getCurrencyNum(CurrencyType.GREEN)
        if (walletAmount < threadHold) {
            if (App.DEBUG) {
                log(TAG, "更新3档人数->当前钱包金额${walletAmount}小于${threadHold}，显示等候人数为0")
            }
            return 0
        }

        var newPlayers = currentPlayers

        // 当总人数小于15时的特殊逻辑
        if (newPlayers < minPlayerCount) {
            // 增加10-20人
            newPlayers += Random.nextInt(10, 21)
            if (App.DEBUG) {
                log(TAG, "更新3档人数->特殊逻辑：总人数小于15，增加10-20人 -> 更新人数${newPlayers}")
            }
            return newPlayers
        } else {
            val arriveRandom = SpUtil.get(SpKey.ARRIVE_LEVEL3_RANDOM, false)
            if (newPlayers < maxPlayerCount && !arriveRandom) {
                // 每分钟增加1-10人，到达100人后开始下面逻辑
                val increase = Random.nextInt(1, 11)
                newPlayers += increase
                if (App.DEBUG) {
                    log(TAG, "更新3档人数->未到达100人，每分钟增加1-10人：增加了${increase}, 更新人数${newPlayers}")
                }
                return newPlayers
            } else {
                //到达过100人，永久随机
                SpUtil.put(SpKey.ARRIVE_LEVEL3_RANDOM, true)
                // 50%概率减少1-5人，50%概率增加1-3人
                if (Random.nextBoolean()) {
                    // 减少1-5人
                    val decrease = Random.nextInt(1, 6)
                    newPlayers = maxOf(0, newPlayers - decrease)
                    if (App.DEBUG) {
                        log(TAG, "更新3档人数-> 50%概率减少5-1人：减少了$decrease")
                    }
                } else {
                    // 增加1-3人
                    val increase = Random.nextInt(1, 4)
                    newPlayers += increase
                    if (App.DEBUG) {
                        log(TAG, "更新3档人数-> 50%概率减少3-1人：减少了$increase")
                    }
                }
                if (App.DEBUG) {
                    log(TAG, "更新3档人数->当前人数：$currentPlayers -> 更新人数：$newPlayers")
                }
                return newPlayers
            }
        }
    }

    /**
     * 获取$299档位的默认随机人数（500-1000）
     */
    fun getDefaultLevel1Players(): Int {
        return Random.nextInt(defaultMinPlayers, defaultMaxPlayers + 1)
    }
}