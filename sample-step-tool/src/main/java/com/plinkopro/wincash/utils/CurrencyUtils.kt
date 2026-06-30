package com.plinkopro.wincash.utils

import app.allever.android.lib.core.app.App
import com.plinkopro.wincash.beans.CurrencyType
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow

//货币工具类
object CurrencyUtils {

    var goldNum = 0f
    var greenNum = 0f

    fun getCurrencyNum(type: CurrencyType): Float {
        return when (type) {
            CurrencyType.GOLD -> goldNum.takeIf { it > 0f }
                ?: SpUtil.get(SpKey.CURRENCY_GOLD_NUM, 0f).also { goldNum = it }

            CurrencyType.GREEN -> greenNum.takeIf { it > 0 }
                ?: SpUtil.get(SpKey.CURRENCY_GREEN_NUM, 0f).also { greenNum = it }
        }
    }

    //更新货币
    fun updateCurrencyNum(type: CurrencyType, num: Float) {
        getCurrencyNum(type)
        when (type) {
            CurrencyType.GOLD -> {
                goldNum += num
                SpUtil.put(SpKey.CURRENCY_GOLD_NUM, goldNum)

            }

            CurrencyType.GREEN -> {
                greenNum += num
                SpUtil.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
            }
        }
    }

    fun computeGoldNum(): Float {
        val menkai = if (goldNum < 5000f) 5000f else 10000f
        val rdmRate = (goldNum / menkai).format4f()

        val ratioTriple = if (rdmRate < 0.3) Triple(0.3, 0.3, 0)
        else if (rdmRate < 0.8) Triple(0.05, 0.1, 0)
        else if (rdmRate < 0.99) Triple(0.1, 0.2, 0)
        else Triple(0.01, 0.02, 2)

        val radio =
            ((ratioTriple.first * 10000).toInt()..(ratioTriple.second * 10000).toInt()).random() / 10000f

        val difference = abs(goldNum - menkai)
        val num = difference * radio

        //保留指定小数点位数，并向上取值
        val numF = ceilToDecimalPlaces(num,ratioTriple.third)
        if (App.DEBUG) {
            LogUtil.local(
                "计算金币规则：距离当前提现金额门槛所差金币 * 补正系数,如果保留0位小数时金币结果为0 则取1,如果保留两位小数时结果小于0.01 则取0.01 " +
                        "\n当前金币数：$goldNum,门槛金币：$menkai, 提现达标率: $rdmRate,补正系数下限：${ratioTriple.first}," +
                        "上限：${ratioTriple.second},所差金币：$difference, 随机补正系数结果：$radio, " +
                        "保留小数位数：${ratioTriple.third},计算结果：$num,保留指定小数位数后最终数量：$numF"
            )
        }
        return numF
    }

    /**8
     * 追加货币
     */
    fun appendCurrencyNum(type: CurrencyType, num: Int) {
        goldNum = SpUtil.get(SpKey.CURRENCY_GOLD_NUM, 0f)
        greenNum = SpUtil.get(SpKey.CURRENCY_GREEN_NUM, 0f)
        when (type) {
            CurrencyType.GOLD -> {
                goldNum += num
                SpUtil.put(SpKey.CURRENCY_GOLD_NUM, goldNum)
            }

            CurrencyType.GREEN -> {
                greenNum += num
                SpUtil.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
            }
        }
    }

    /***
     * 减少货币
     */
    fun reduceCurrencyNum(type: CurrencyType, num: Int) {
        goldNum = SpUtil.get(SpKey.CURRENCY_GOLD_NUM, 0f)
        greenNum = SpUtil.get(SpKey.CURRENCY_GREEN_NUM, 0f)
        when (type) {
            CurrencyType.GOLD -> {
                goldNum -= num
                if (goldNum < 0) goldNum = 0f
                SpUtil.put(SpKey.CURRENCY_GOLD_NUM, goldNum)
            }

            CurrencyType.GREEN -> {
                greenNum -= num
                if (greenNum < 0) greenNum = 0f
                SpUtil.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
            }
        }
    }

    fun clearCurrencyNum() {
        goldNum = 0f
        greenNum = 0f
        SpUtil.put(SpKey.CURRENCY_GOLD_NUM, goldNum)
        SpUtil.put(SpKey.CURRENCY_GREEN_NUM, greenNum)
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