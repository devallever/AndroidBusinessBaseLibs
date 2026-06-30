package com.plinkopro.wincash.business.withdraw

import androidx.lifecycle.MutableLiveData
import app.allever.android.lib.core.app.App

import com.plinkopro.wincash.beans.CountryConfig
import com.plinkopro.wincash.beans.CurrencyType
import com.plinkopro.wincash.beans.LocalWithdrawRecord
import com.plinkopro.wincash.beans.WithdrawRecord
import com.plinkopro.wincash.init.FpManger
import com.plinkopro.wincash.init.InitManager
import com.plinkopro.wincash.utils.GsonUtil
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.utils.asLiveData
import com.plinkopro.wincash.utils.log
import com.plinkopro.wincash.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

import kotlin.random.Random

object WithdrawBusiness {
    const val WITHDRAW_LEVEL_SMALL = 0
    const val WITHDRAW_LEVEL_1 = 1
    const val WITHDRAW_LEVEL_2 = 2

    private val RANK_START = 18000 //18000
    private val RANK_END = 20000 //20000

    private val stepConfig = FpManger.stepConfig
    val countryConfigMap = mutableMapOf<String, CountryConfig>()

    val DEFAULT_COUNTRY = CountryConfig()

    fun updateConfig() {
        for (countryConfig in stepConfig.countryList) {
            countryConfigMap[countryConfig.countryCode] = countryConfig
        }

        if (App.DEBUG) {
            log("WithdrawBusiness", "获取国家配置列表：${countryConfigMap.toJson()}")
        }
    }

    private val _recordListLiveData = MutableLiveData<MutableList<WithdrawRecord>>().apply {
        val gson = SpUtil.get(SpKey.WITHDRAW_RECORD_LIST, "")
        value = try {
            if (gson.isEmpty()) {
                mutableListOf()
            } else {
                // 尝试反序列化，如果失败则返回空列表
                val localRecord = GsonUtil.fromJson(gson, LocalWithdrawRecord::class.java)
                localRecord?.list ?: mutableListOf()
            }
        } catch (e: Exception) {
            // 如果遇到任何异常，记录日志并返回空列表
            if (App.DEBUG) {
                log("WithdrawBusiness", "反序列化提现记录失败: ${e.message}")
            }
            mutableListOf()
        }
        if (App.DEBUG) {
            log("WithdrawBusiness", "获取提现记录列表：$gson")
        }
    }
    val recordListLiveData = _recordListLiveData.asLiveData

    /**
     * 生成6位包含大写字母和数字的随机字符串
     *
     */
    fun generateRandomUserId(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { chars[Random.nextInt(chars.size)] }.joinToString("")
    }

    /***
     * 获取提现限制, 超过后能点提现，也是提现后扣掉的货币值
     */
    fun getWithdrawCurrencyLimit(currencyType: CurrencyType, level: Int): Int {
        val countryCode = InitManager.getCountryCode()
        val country = countryConfigMap[countryCode] ?: DEFAULT_COUNTRY
        when (currencyType) {
            CurrencyType.GOLD -> {
                return if (level == WITHDRAW_LEVEL_1) {
                    country.largeGoldLevel1
                } else {
                    country.largeGoldLevel2
                }
            }

            CurrencyType.GREEN -> {
                return if (level == WITHDRAW_LEVEL_1) {
                    country.largeGreenLevel1
                } else {
                    country.largeGreenLevel2
                }
            }
        }
    }

    /**
     * 获取卡片上显示的货币值，每个国家不同的, 金币和绿钞都一样
     */
    fun getWithdrawCurrencyLabelValue(countryCode: String, level: Int): Int {
        val country = countryConfigMap[countryCode] ?: DEFAULT_COUNTRY
        return if (level == WITHDRAW_LEVEL_1) {
            country.largeAmountLevel1
        } else {
            country.largeAmountLevel2
        }
    }

    fun getStartRank(): Int {
        return Random.nextInt(RANK_START, RANK_END)
    }

    fun createRankEndPoint(): Int {
        return Random.nextInt(50, 101)
    }

    /**
     * 获取下一次排队人数
     */
    fun getNextRank(currentRank: Int, queueEndPoint: Int, finish: () -> Unit): Int {
        val reduceCount = when {
            currentRank > 9000 -> Random.nextInt(8, 12) // random(8,11) 闭区间
            currentRank > 8000 -> Random.nextInt(7, 11) // random(7,10)
            currentRank > 7000 -> Random.nextInt(6, 10) // random(6,9)
            currentRank > 6000 -> Random.nextInt(5, 9)  // random(5,8)
            currentRank > 5000 -> Random.nextInt(4, 8)  // random(4,7)
            currentRank > 4000 -> Random.nextInt(3, 7)  // random(3,6)
            currentRank > 3000 -> Random.nextInt(2, 6)  // random(2,5)
            currentRank > 2000 -> Random.nextInt(1, 5)  // random(1,4)
            currentRank > 1000 -> Random.nextInt(1, 4)  // random(1,3)
            currentRank > queueEndPoint -> 1                  // random(1,1)
            else -> 0
        }

        val nextRank = currentRank - reduceCount
        if (App.DEBUG) {
            log(
                "WithdrawBusiness",
                "当前排队人数：$currentRank，应减少人数：$reduceCount，下一级排队人数：$nextRank"
            )
        }
        if (reduceCount == 0 || nextRank == queueEndPoint) {
            if (App.DEBUG) {
                log(
                    "WithdrawBusiness",
                    "到达终点：$nextRank"
                )
            }
            finish.invoke()
        }
        return nextRank
    }

    /**
     * 加速排队, 观看广告调用，返回新的排队人数
     */
    fun accelerateRank(currentCount: Int, queueEndPoint: Int, finish: () -> Unit): Int {

        // 如果已经到达或低于终点人数，无法加速
        if (currentCount <= queueEndPoint) {
            finish.invoke()
            return currentCount
        }

        // 计算剩余进度（当前人数 - 终点人数）
        val remainingProgress = currentCount - queueEndPoint

        // 计算加速减少的人数（剩余进度的1%，取整数）
        val accelerateReduceCount = (remainingProgress * 0.01).toInt()

        // 确保至少减少1人
        val actualReduceCount = maxOf(accelerateReduceCount, 1)

        // 计算新的排队人数，确保不低于终点人数
        val newCount = maxOf(currentCount - actualReduceCount, queueEndPoint)

        return newCount
    }

    /**
     * 加速排队, 观看广告调用，返回新的排队人数
     */
    fun accelerateRank(withdrawRecord: WithdrawRecord, finish: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            // 如果已经到达或低于终点人数，无法加速
            if (withdrawRecord.rank <= withdrawRecord.endRank) {
                finish.invoke()
                return@launch
            }

            // 计算剩余进度（当前人数 - 终点人数）
            val remainingProgress = withdrawRecord.rank/* - withdrawRecord.endRank*/

            // 计算加速减少的人数（剩余进度的1%，取整数）
            val accelerateReduceCount = (remainingProgress * 0.01).toInt()

            // 确保至少减少1人
            val actualReduceCount = maxOf(accelerateReduceCount, 1)

            // 计算新的排队人数，确保不低于终点人数
            val newCount = maxOf(withdrawRecord.rank - actualReduceCount, withdrawRecord.endRank)

            var updated = false
            _recordListLiveData.value?.forEach {
                if (it.time == withdrawRecord.time) {
                    it.rank = newCount
                    updated = true
                }
            }

            if (updated) {
                if (App.DEBUG) {
                    log("加速排名, 当前：${newCount}")
                }
                saveRecordList()
            }
        }

    }

    fun updateRank() {
        CoroutineScope(Dispatchers.IO).launch {
            var updated = false
            val oldList = _recordListLiveData.value
            oldList?.map {
                if (!it.finish) {
                    val nextRank = getNextRank(it.rank, it.endRank) {
                        it.finish = true
                    }
                    it.rank = nextRank
                    updated = true
                }
            }
            if (updated) {
                saveRecordList()
            }
        }
    }

    fun saveRecordList() {
        try {
            _recordListLiveData.value?.let {
                val updateJson = GsonUtil.toJson(LocalWithdrawRecord(it))
                if (updateJson.isNotEmpty()) {
                    SpUtil.put(SpKey.WITHDRAW_RECORD_LIST, updateJson)
                }
                _recordListLiveData.postValue(it)
                if (App.DEBUG) {
                    log("WithdrawBusiness", "更新提现排名记录：${updateJson}")
                }
            }
        } catch (e: Exception) {
            // 捕获所有异常，确保不会崩溃
            if (App.DEBUG) {
                log("WithdrawBusiness", "保存记录列表失败: ${e.message}")
            }
        }
    }

    fun insertRecord(record: WithdrawRecord, finish: () -> Unit= {}) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _recordListLiveData.value?.add(0, record)
                _recordListLiveData.value?.let {
                    val json = GsonUtil.toJson(LocalWithdrawRecord(it))
                    if (json.isNotEmpty()) {
                        SpUtil.put(SpKey.WITHDRAW_RECORD_LIST, json)
                    }
                    finish.invoke()
                    // 确保在主线程更新LiveData
                    _recordListLiveData.postValue(it)
                }
            } catch (e: Exception) {
                // 捕获所有异常，确保不会崩溃
                if (App.DEBUG) {
                    log("WithdrawBusiness", "保存提现记录失败: ${e.message}")
                }
                finish.invoke()
            }
        }
    }

}
