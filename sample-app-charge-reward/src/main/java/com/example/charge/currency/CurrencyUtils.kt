package com.example.charge.currency

import com.example.charge.init.FpManger
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil
import kotlin.math.ceil
import kotlin.math.pow

//货币工具类
object CurrencyUtils {

    var goldNum = 0f
    var greenNum = 0f

    fun getCurrencyNum(type: CurrencyType): Float {
        return when (type) {
            CurrencyType.GOLD -> goldNum.takeIf { it > 0f }
                ?: SpUtil.Companion.get(SpKey.CURRENCY_GOLD_NUM, 0f).also { goldNum = it }

            CurrencyType.GREEN -> greenNum.takeIf { it > 0 }
                ?: SpUtil.Companion.get(SpKey.CURRENCY_GREEN_NUM, 0f).also { greenNum = it }
        }
    }

    //更新货币
    fun updateCurrencyNum(type: CurrencyType, num: Float) {
        getCurrencyNum(type)
        when (type) {
            CurrencyType.GOLD -> {
                goldNum += num
                SpUtil.Companion.put(SpKey.CURRENCY_GOLD_NUM, goldNum)
            }

            CurrencyType.GREEN -> {
                greenNum += num
                SpUtil.Companion.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
                dotUserCash(greenNum)
            }
        }

    }

    private fun dotUserCash(greenNum: Float) {
        val levelList = FpManger.chargeConfig.withdraw
        var level = 0
        if (levelList.size > 2 && greenNum > levelList[2].limit) {
            level = 3
        } else if (levelList.size > 1 && greenNum > levelList[1].limit) {
            level = 2
        } else if (levelList.isNotEmpty() && greenNum > levelList[0].limit) {
            level = 1
        } else {
            level = 0
        }

    }

    /**8
     * 追加货币
     */
    fun appendCurrencyNum(type: CurrencyType, num: Int) {
        goldNum = SpUtil.Companion.get(SpKey.CURRENCY_GOLD_NUM, 0f)
        greenNum = SpUtil.Companion.get(SpKey.CURRENCY_GREEN_NUM, 0f)
        when (type) {
            CurrencyType.GOLD -> {
                goldNum += num
                SpUtil.Companion.put(SpKey.CURRENCY_GOLD_NUM, goldNum)
            }

            CurrencyType.GREEN -> {
                greenNum += num
                SpUtil.Companion.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
            }
        }
    }

    /***
     * 减少货币
     */
    fun reduceCurrencyNum(type: CurrencyType, num: Int) {
        goldNum = SpUtil.Companion.get(SpKey.CURRENCY_GOLD_NUM, 0f)
        greenNum = SpUtil.Companion.get(SpKey.CURRENCY_GREEN_NUM, 0f)
        when (type) {
            CurrencyType.GOLD -> {
                goldNum -= num
                if (goldNum < 0) goldNum = 0f
                SpUtil.Companion.put(SpKey.CURRENCY_GOLD_NUM, goldNum)
            }

            CurrencyType.GREEN -> {
                greenNum -= num
                if (greenNum < 0) greenNum = 0f
                SpUtil.Companion.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
            }
        }
    }

    fun clearCurrencyNum() {
        goldNum = 0f
        greenNum = 0f
        SpUtil.Companion.put(SpKey.CURRENCY_GOLD_NUM, goldNum)
        SpUtil.Companion.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
    }

    /**
     * 向上取整到指定小数位数
     * @param value 需要处理的浮点数
     * @param decimalPlaces 保留的小数位数
     * @return 向上取整后的结果
     */
    fun ceilToDecimalPlaces(value: Float, decimalPlaces: Int): Float {
        val multiplier = 10.0.pow(decimalPlaces).toFloat()
        return ceil(value * multiplier) / multiplier
    }
}