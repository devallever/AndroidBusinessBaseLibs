package com.example.charge.task

import app.allever.android.lib.core.app.App
import com.example.charge.ChargeApp
import com.example.charge.R
import com.example.charge.data.TaskItem
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil
import com.example.charge.utils.log
import com.example.charge.utils.moveElementsToEnd
import com.example.charge.vm.VMHelper
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager

object TaskHelper {
    val chargeItemList = mutableListOf<TaskItem>().apply {
        // 收集气泡任务
        add(TaskItem(TaskId.CHARGE_COLLECT_20, TaskType.CHARGE, TaskCategory.CHARGE_COLLECT,1f, "", 20))
        add(TaskItem(TaskId.CHARGE_COLLECT_100, TaskType.CHARGE, TaskCategory.CHARGE_COLLECT,5f, "", 100))
        add(TaskItem(TaskId.CHARGE_COLLECT_200, TaskType.CHARGE, TaskCategory.CHARGE_COLLECT,10f, "", 200))
        add(TaskItem(TaskId.CHARGE_COLLECT_400, TaskType.CHARGE, TaskCategory.CHARGE_COLLECT,20f, "", 400))
        // 签到任务
        add(TaskItem(TaskId.CHARGE_SIGN_1, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,0.2f, "", 1))
        add(TaskItem(TaskId.CHARGE_SIGN_2, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,0.5f, "", 2))
        add(TaskItem(TaskId.CHARGE_SIGN_3, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,1f, "", 3))
        add(TaskItem(TaskId.CHARGE_SIGN_4, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,1.5f, "", 4))
        add(TaskItem(TaskId.CHARGE_SIGN_5, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,2f, "", 5))
        add(TaskItem(TaskId.CHARGE_SIGN_6, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,3f, "", 6))
        add(TaskItem(TaskId.CHARGE_SIGN_7, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,4f, "", 7))
        add(TaskItem(TaskId.CHARGE_SIGN_8, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,5f, "", 8))
        add(TaskItem(TaskId.CHARGE_SIGN_9, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,6f, "", 9))
        add(TaskItem(TaskId.CHARGE_SIGN_10, TaskType.CHARGE, TaskCategory.CHARGE_SIGN,7f, "", 10))
        this.moveElementsToEnd {
            it.isFinished
        }
    }
    val hitMoleItemList = mutableListOf<TaskItem>().apply {
        // 打地鼠数量任务
        add(TaskItem(TaskId.HIT_MOLE_50, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE,1f, "", 50))
        add(TaskItem(TaskId.HIT_MOLE_300, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE,5f, "", 300))
        add(TaskItem(TaskId.HIT_MOLE_1000, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE,10f, "", 1000))
        add(TaskItem(TaskId.HIT_MOLE_3000, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE,20f, "", 3000))
        add(TaskItem(TaskId.HIT_MOLE_10000, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE,40f, "", 10000))
        // 打地鼠游戏次数任务
        add(TaskItem(TaskId.HIT_MOLE_GAME_5, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE_GAME_COUNT,1f, "", 5))
        add(TaskItem(TaskId.HIT_MOLE_GAME_15, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE_GAME_COUNT,2f, "", 15))
        add(TaskItem(TaskId.HIT_MOLE_GAME_30, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE_GAME_COUNT,3f, "", 30))
        add(TaskItem(TaskId.HIT_MOLE_GAME_50, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE_GAME_COUNT,4f, "", 50))
        add(TaskItem(TaskId.HIT_MOLE_GAME_80, TaskType.HIT_MOLE, TaskCategory.HIT_MOLE_GAME_COUNT,5f, "", 80))
        this.moveElementsToEnd {
            it.isFinished
        }
    }
    val receiveCoinItemList = mutableListOf<TaskItem>().apply {
        // 接金币数量任务
        add(TaskItem(TaskId.RECEIVE_COIN_200, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN,1f, "", 200))
        add(TaskItem(TaskId.RECEIVE_COIN_1000, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN,5f, "", 1000))
        add(TaskItem(TaskId.RECEIVE_COIN_2500, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN,10f, "", 2500))
        add(TaskItem(TaskId.RECEIVE_COIN_5000, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN,20f, "", 5000))
        add(TaskItem(TaskId.RECEIVE_COIN_10000, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN,40f, "", 10000))
        // 接金币游戏次数任务
        add(TaskItem(TaskId.RECEIVE_COIN_GAME_5, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN_GAME_COUNT,1f, "", 5))
        add(TaskItem(TaskId.RECEIVE_COIN_GAME_15, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN_GAME_COUNT,2f, "", 15))
        add(TaskItem(TaskId.RECEIVE_COIN_GAME_30, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN_GAME_COUNT,3f, "", 30))
        add(TaskItem(TaskId.RECEIVE_COIN_GAME_50, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN_GAME_COUNT,4f, "", 50))
        add(TaskItem(TaskId.RECEIVE_COIN_GAME_80, TaskType.RECEIVE_COIN, TaskCategory.RECEIVE_COIN_GAME_COUNT,5f, "", 80))
        this.moveElementsToEnd {
            it.isFinished
        }
    }

    val idTaskMap = mutableMapOf<String, TaskItem>().apply {
        chargeItemList.forEach {
            put(it.id, it)
        }
        hitMoleItemList.forEach {
            put(it.id, it)
        }
        receiveCoinItemList.forEach {
            put(it.id, it)
        }
    }


    /**
     * 更新数量，领取后都要检查是否显示小圆点
     * @param taskType 任务类型TaskType.CHARGE, TaskType.HIT_MOLE, TaskType.RECEIVE_COIN
     */
    fun checkShowDot(taskType: String = "") {
        if (taskType.isNotEmpty()) {
            when (taskType) {
                TaskType.CHARGE -> checkShowItem(chargeItemList)
                TaskType.HIT_MOLE -> checkShowItem(hitMoleItemList)
                TaskType.RECEIVE_COIN -> checkShowItem(receiveCoinItemList)
            }
            return
        }

        var show = false
        show = checkShowItem(chargeItemList)
        if (show) {
            return
        }
        show = checkShowItem(hitMoleItemList)
        if (show) {
            return
        }
        show = checkShowItem(receiveCoinItemList)
        if (show) {
            return
        }

        VMHelper.taskViewModel.showTaskDot.value = false

    }
    private fun checkShowItem(list: MutableList<TaskItem>): Boolean {
        list.forEach {
            if (!it.isFinished && it.current >= it.max) {
                VMHelper.taskViewModel.showTaskDot.value = true
                return true
            }
        }
        return false
    }

    fun getTaskProgress(taskId: String): Int {
        return when(taskId) {
            // 充电收集气泡任务进度
            TaskId.CHARGE_COLLECT_20,
            TaskId.CHARGE_COLLECT_100,
            TaskId.CHARGE_COLLECT_200,
            TaskId.CHARGE_COLLECT_400 -> {
                getCollectCount()
            }
            // 签到任务进度
            TaskId.CHARGE_SIGN_1,
            TaskId.CHARGE_SIGN_2,
            TaskId.CHARGE_SIGN_3,
            TaskId.CHARGE_SIGN_4,
            TaskId.CHARGE_SIGN_5,
            TaskId.CHARGE_SIGN_6,
            TaskId.CHARGE_SIGN_7,
            TaskId.CHARGE_SIGN_8,
            TaskId.CHARGE_SIGN_9,
            TaskId.CHARGE_SIGN_10 -> {
                getSignCount()
            }
            // 打地鼠数量任务进度
            TaskId.HIT_MOLE_50,
            TaskId.HIT_MOLE_300,
            TaskId.HIT_MOLE_1000,
            TaskId.HIT_MOLE_3000,
            TaskId.HIT_MOLE_10000 -> {
                getHitMoleCount()
            }
            // 打地鼠游戏次数任务进度
            TaskId.HIT_MOLE_GAME_5,
            TaskId.HIT_MOLE_GAME_15,
            TaskId.HIT_MOLE_GAME_30,
            TaskId.HIT_MOLE_GAME_50,
            TaskId.HIT_MOLE_GAME_80 -> {
                getHitMoleGameCount()
            }
            // 接金币数量任务进度
            TaskId.RECEIVE_COIN_200,
            TaskId.RECEIVE_COIN_1000,
            TaskId.RECEIVE_COIN_2500,
            TaskId.RECEIVE_COIN_5000,
            TaskId.RECEIVE_COIN_10000 -> {
                getReceiveCoinCount()
            }
            // 接金币游戏次数任务进度
            TaskId.RECEIVE_COIN_GAME_5,
            TaskId.RECEIVE_COIN_GAME_15,
            TaskId.RECEIVE_COIN_GAME_30,
            TaskId.RECEIVE_COIN_GAME_50,
            TaskId.RECEIVE_COIN_GAME_80 -> {
                getReceiveCoinGameCount()
            }
            else -> 0
        }
    }

    fun checkFinish(id: String): Boolean {
        return when(id) {
            // 充电收集气泡任务完成状态
            TaskId.CHARGE_COLLECT_20 -> getCollectFinish(TaskId.CHARGE_COLLECT_20)
            TaskId.CHARGE_COLLECT_100 -> getCollectFinish(TaskId.CHARGE_COLLECT_100)
            TaskId.CHARGE_COLLECT_200 -> getCollectFinish(TaskId.CHARGE_COLLECT_200)
            TaskId.CHARGE_COLLECT_400 -> getCollectFinish(TaskId.CHARGE_COLLECT_400)

            // 签到任务完成状态
            TaskId.CHARGE_SIGN_1 -> getSignFinish(TaskId.CHARGE_SIGN_1)
            TaskId.CHARGE_SIGN_2 -> getSignFinish(TaskId.CHARGE_SIGN_2)
            TaskId.CHARGE_SIGN_3 -> getSignFinish(TaskId.CHARGE_SIGN_3)
            TaskId.CHARGE_SIGN_4 -> getSignFinish(TaskId.CHARGE_SIGN_4)
            TaskId.CHARGE_SIGN_5 -> getSignFinish(TaskId.CHARGE_SIGN_5)
            TaskId.CHARGE_SIGN_6 -> getSignFinish(TaskId.CHARGE_SIGN_6)
            TaskId.CHARGE_SIGN_7 -> getSignFinish(TaskId.CHARGE_SIGN_7)
            TaskId.CHARGE_SIGN_8 -> getSignFinish(TaskId.CHARGE_SIGN_8)
            TaskId.CHARGE_SIGN_9 -> getSignFinish(TaskId.CHARGE_SIGN_9)
            TaskId.CHARGE_SIGN_10 -> getSignFinish(TaskId.CHARGE_SIGN_10)

            // 打地鼠数量任务完成状态
            TaskId.HIT_MOLE_50 -> getHitMoleFinish(TaskId.HIT_MOLE_50)
            TaskId.HIT_MOLE_300 -> getHitMoleFinish(TaskId.HIT_MOLE_300)
            TaskId.HIT_MOLE_1000 -> getHitMoleFinish(TaskId.HIT_MOLE_1000)
            TaskId.HIT_MOLE_3000 -> getHitMoleFinish(TaskId.HIT_MOLE_3000)
            TaskId.HIT_MOLE_10000 -> getHitMoleFinish(TaskId.HIT_MOLE_10000)

            // 打地鼠游戏次数任务完成状态
            TaskId.HIT_MOLE_GAME_5 -> getHitMoleGameFinish(TaskId.HIT_MOLE_GAME_5)
            TaskId.HIT_MOLE_GAME_15 -> getHitMoleGameFinish(TaskId.HIT_MOLE_GAME_15)
            TaskId.HIT_MOLE_GAME_30 -> getHitMoleGameFinish(TaskId.HIT_MOLE_GAME_30)
            TaskId.HIT_MOLE_GAME_50 -> getHitMoleGameFinish(TaskId.HIT_MOLE_GAME_50)
            TaskId.HIT_MOLE_GAME_80 -> getHitMoleGameFinish(TaskId.HIT_MOLE_GAME_80)

            // 接金币数量任务完成状态
            TaskId.RECEIVE_COIN_200 -> getReceiveCoinFinish(TaskId.RECEIVE_COIN_200)
            TaskId.RECEIVE_COIN_1000 -> getReceiveCoinFinish(TaskId.RECEIVE_COIN_1000)
            TaskId.RECEIVE_COIN_2500 -> getReceiveCoinFinish(TaskId.RECEIVE_COIN_2500)
            TaskId.RECEIVE_COIN_5000 -> getReceiveCoinFinish(TaskId.RECEIVE_COIN_5000)
            TaskId.RECEIVE_COIN_10000 -> getReceiveCoinFinish(TaskId.RECEIVE_COIN_10000)

            // 接金币游戏次数任务完成状态
            TaskId.RECEIVE_COIN_GAME_5 -> getReceiveCoinGameFinish(TaskId.RECEIVE_COIN_GAME_5)
            TaskId.RECEIVE_COIN_GAME_15 -> getReceiveCoinGameFinish(TaskId.RECEIVE_COIN_GAME_15)
            TaskId.RECEIVE_COIN_GAME_30 -> getReceiveCoinGameFinish(TaskId.RECEIVE_COIN_GAME_30)
            TaskId.RECEIVE_COIN_GAME_50 -> getReceiveCoinGameFinish(TaskId.RECEIVE_COIN_GAME_50)
            TaskId.RECEIVE_COIN_GAME_80 -> getReceiveCoinGameFinish(TaskId.RECEIVE_COIN_GAME_80)

            else -> false
        }
    }

    fun getBtnText(taskId: String): String {
        return when (taskId) {
            TaskId.CHARGE_COLLECT_20,
            TaskId.CHARGE_COLLECT_100,
            TaskId.CHARGE_COLLECT_200,
            TaskId.CHARGE_COLLECT_400 -> {
                ChargeApp.instance.getString(R.string.task_btn_state_collect)
            }
            TaskId.CHARGE_SIGN_1,
            TaskId.CHARGE_SIGN_2,
            TaskId.CHARGE_SIGN_3,
            TaskId.CHARGE_SIGN_4,
            TaskId.CHARGE_SIGN_5,
            TaskId.CHARGE_SIGN_6,
            TaskId.CHARGE_SIGN_7,
            TaskId.CHARGE_SIGN_8,
            TaskId.CHARGE_SIGN_9,
            TaskId.CHARGE_SIGN_10 -> {
                ChargeApp.instance.getString(R.string.task_btn_state_sign)
            }
            TaskId.HIT_MOLE_50,
            TaskId.HIT_MOLE_300,
            TaskId.HIT_MOLE_1000,
            TaskId.HIT_MOLE_3000,
            TaskId.HIT_MOLE_10000,
            TaskId.HIT_MOLE_GAME_5,
            TaskId.HIT_MOLE_GAME_15,
            TaskId.HIT_MOLE_GAME_30,
            TaskId.HIT_MOLE_GAME_50,
            TaskId.HIT_MOLE_GAME_80,
                 -> {
                ChargeApp.instance.getString(R.string.task_btn_state_hit)
            }
            TaskId.RECEIVE_COIN_200,
            TaskId.RECEIVE_COIN_1000,
            TaskId.RECEIVE_COIN_2500,
            TaskId.RECEIVE_COIN_5000,
            TaskId.RECEIVE_COIN_10000,
            TaskId.RECEIVE_COIN_GAME_5,
            TaskId.RECEIVE_COIN_GAME_15,
            TaskId.RECEIVE_COIN_GAME_30,
            TaskId.RECEIVE_COIN_GAME_50,
            TaskId.RECEIVE_COIN_GAME_80
                 -> {
                ChargeApp.instance.getString(R.string.task_btn_state_receive)
            }
            else -> ""
        }
    }

    fun getTaskDesc(taskId: String, value: Int): String {
        val valueString = value.toString()
        when (taskId) {
            TaskId.CHARGE_COLLECT_20,
            TaskId.CHARGE_COLLECT_100,
            TaskId.CHARGE_COLLECT_200,
            TaskId.CHARGE_COLLECT_400 -> {
                return ChargeApp.instance.getString(R.string.task_desc_charge_collect, valueString)
            }

            TaskId.CHARGE_SIGN_1,
            TaskId.CHARGE_SIGN_2,
            TaskId.CHARGE_SIGN_3,
            TaskId.CHARGE_SIGN_4,
            TaskId.CHARGE_SIGN_5,
            TaskId.CHARGE_SIGN_6,
            TaskId.CHARGE_SIGN_7,
            TaskId.CHARGE_SIGN_8,
            TaskId.CHARGE_SIGN_9,
            TaskId.CHARGE_SIGN_10 -> {
                return ChargeApp.instance.getString(R.string.task_desc_charge_sign, valueString)
            }
            // 打地鼠任务描述
            TaskId.HIT_MOLE_50,
            TaskId.HIT_MOLE_300,
            TaskId.HIT_MOLE_1000,
            TaskId.HIT_MOLE_3000,
            TaskId.HIT_MOLE_10000 -> {
                return ChargeApp.instance.getString(R.string.task_desc_mole_hit, valueString)
            }

            TaskId.HIT_MOLE_GAME_5,
            TaskId.HIT_MOLE_GAME_15,
            TaskId.HIT_MOLE_GAME_30,
            TaskId.HIT_MOLE_GAME_50,
            TaskId.HIT_MOLE_GAME_80 -> {
                return ChargeApp.instance.getString(R.string.task_desc_game_count, valueString)
            }
            // 接金币任务描述
            TaskId.RECEIVE_COIN_200,
            TaskId.RECEIVE_COIN_1000,
            TaskId.RECEIVE_COIN_2500,
            TaskId.RECEIVE_COIN_5000,
            TaskId.RECEIVE_COIN_10000 -> {
                return ChargeApp.instance.getString(R.string.task_desc_coin_receive, valueString)
            }

            TaskId.RECEIVE_COIN_GAME_5,
            TaskId.RECEIVE_COIN_GAME_15,
            TaskId.RECEIVE_COIN_GAME_30,
            TaskId.RECEIVE_COIN_GAME_50,
            TaskId.RECEIVE_COIN_GAME_80 -> {
                return ChargeApp.instance.getString(R.string.task_desc_game_count, valueString)
            }

            else -> {
                return ""
            }
        }
    }

    fun getCollectCount(): Int {
        return SpUtil.get(SpKey.TASK_CHARGE_COLLECT_COUNT, 0)
    }

    fun getCurrentCount(taskId: String): Int {
        return when(taskId) {
            // 充电收集气泡任务当前数量
            TaskId.CHARGE_COLLECT_20,
            TaskId.CHARGE_COLLECT_100,
            TaskId.CHARGE_COLLECT_200,
            TaskId.CHARGE_COLLECT_400 -> {
                getCollectCount()
            }
            // 签到任务当前数量
            TaskId.CHARGE_SIGN_1,
            TaskId.CHARGE_SIGN_2,
            TaskId.CHARGE_SIGN_3,
            TaskId.CHARGE_SIGN_4,
            TaskId.CHARGE_SIGN_5,
            TaskId.CHARGE_SIGN_6,
            TaskId.CHARGE_SIGN_7,
            TaskId.CHARGE_SIGN_8,
            TaskId.CHARGE_SIGN_9,
            TaskId.CHARGE_SIGN_10 -> {
                getSignCount()
            }
            // 打地鼠数量任务当前数量
            TaskId.HIT_MOLE_50,
            TaskId.HIT_MOLE_300,
            TaskId.HIT_MOLE_1000,
            TaskId.HIT_MOLE_3000,
            TaskId.HIT_MOLE_10000 -> {
                getHitMoleCount()
            }
            // 打地鼠游戏次数任务当前数量
            TaskId.HIT_MOLE_GAME_5,
            TaskId.HIT_MOLE_GAME_15,
            TaskId.HIT_MOLE_GAME_30,
            TaskId.HIT_MOLE_GAME_50,
            TaskId.HIT_MOLE_GAME_80 -> {
                getHitMoleGameCount()
            }
            // 接金币数量任务当前数量
            TaskId.RECEIVE_COIN_200,
            TaskId.RECEIVE_COIN_1000,
            TaskId.RECEIVE_COIN_2500,
            TaskId.RECEIVE_COIN_5000,
            TaskId.RECEIVE_COIN_10000 -> {
                getReceiveCoinCount()
            }
            // 接金币游戏次数任务当前数量
            TaskId.RECEIVE_COIN_GAME_5,
            TaskId.RECEIVE_COIN_GAME_15,
            TaskId.RECEIVE_COIN_GAME_30,
            TaskId.RECEIVE_COIN_GAME_50,
            TaskId.RECEIVE_COIN_GAME_80 -> {
                getReceiveCoinGameCount()
            }
            else -> 0
        }
    }

    fun addCollectCount() {
        val value = getCollectCount() + 1
        if (App.DEBUG) {
            log("收集${value}个气泡")
        }
        setCollectCount(value)
    }

    private fun setCollectCount(count: Int) {
        val current = getCollectCount()
        if (current == count) {
            return
        }
        SpUtil.put(SpKey.TASK_CHARGE_COLLECT_COUNT, count)
        chargeItemList.forEach {
            if (it.category == TaskCategory.CHARGE_COLLECT) {
                it.current = count
            }
        }
        checkShowDot(TaskType.CHARGE)
    }

    fun getSignCount(): Int {
        return SpUtil.get(SpKey.TASK_CHARGE_SIGN_COUNT, 0)
    }

    fun setSignCount(count: Int) {
        val current = getSignCount()
        if (current == count) {
            return
        }
        SpUtil.put(SpKey.TASK_CHARGE_SIGN_COUNT, count)
        chargeItemList.forEach {
            if (it.category == TaskCategory.CHARGE_SIGN) {
                it.current = count
            }
        }
        checkShowDot(TaskType.CHARGE)
    }

    fun getHitMoleCount(): Int {
        return SpUtil.get(SpKey.HIT_MOLE_COUNT, 0)
    }

    fun setHitMoleCount(count: Int) {
        if (App.DEBUG) {
            log("打地鼠打中${count}次")
        }
        val current = getHitMoleCount()
        if (current == count) {
            return
        }
        SpUtil.put(SpKey.HIT_MOLE_COUNT, count)
        hitMoleItemList.forEach {
            if (it.category == TaskCategory.HIT_MOLE) {
                it.current = count
            }
        }
        checkShowDot(TaskType.HIT_MOLE)
    }

    fun getReceiveCoinCount(): Int {
        return SpUtil.get(SpKey.RECEIVE_COIN_COUNT, 0)
    }

    fun setReceiveCoinCount(count: Int) {
        if (App.DEBUG) {
            log("接金币接中${count}次")
        }
        val current = getReceiveCoinCount()
        if (current == count) {
            return
        }
        SpUtil.put(SpKey.RECEIVE_COIN_COUNT, count)
        receiveCoinItemList.forEach {
            if (it.category == TaskCategory.RECEIVE_COIN) {
                it.current = count
            }
        }
        checkShowDot(TaskType.RECEIVE_COIN)
    }

    fun getHitMoleGameCount(): Int {
        return SpUtil.get(SpKey.TASK_HIT_MOLE_GAME_COUNT, 0)
    }

    fun setHitMoleGameCount(count: Int) {
        if (App.DEBUG) {
            log("打地鼠游戏${count}次")
        }
        val current = getHitMoleGameCount()
        if (current == count) {
            return
        }
        SpUtil.put(SpKey.TASK_HIT_MOLE_GAME_COUNT, count)
        hitMoleItemList.forEach {
            if (it.category == TaskCategory.HIT_MOLE_GAME_COUNT) {
                it.current = count
            }
        }
    }

    fun getReceiveCoinGameCount(): Int {
        return SpUtil.get(SpKey.TASK_RECEIVE_COIN_GAME_COUNT, 0)
    }

    fun setReceiveCoinGameCount(count: Int) {
        if (App.DEBUG) {
            log("接金币游戏${count}次")
        }
        val current = getReceiveCoinGameCount()
        if (current == count) {
            return
        }
        SpUtil.put(SpKey.TASK_RECEIVE_COIN_GAME_COUNT, count)
        receiveCoinItemList.forEach {
            if (it.category == TaskCategory.RECEIVE_COIN_GAME_COUNT) {
                it.current = count
            }
        }
    }

    fun getCollectFinish(taskId: String): Boolean {
        return SpUtil.get(SpKey.TASK_CHARGE_COLLECT_FINISH + taskId, false)
    }

    fun setCollectFinish(taskId: String) {
        SpUtil.put(SpKey.TASK_CHARGE_COLLECT_FINISH + taskId, true)
    }

    fun getSignFinish(taskId: String): Boolean {
        return SpUtil.get(SpKey.TASK_CHARGE_SIGN_FINISH + taskId, false)
    }

    fun setSignFinish(taskId: String) {
        SpUtil.put(SpKey.TASK_CHARGE_SIGN_FINISH + taskId, true)
    }

    fun getHitMoleFinish(taskId: String): Boolean {
        return SpUtil.get(SpKey.TASK_HIT_MOLE_FINISH + taskId, false)
    }

    fun setHitMoleFinish(taskId: String) {
        SpUtil.put(SpKey.TASK_HIT_MOLE_FINISH + taskId, true)
    }

    fun getReceiveCoinFinish(taskId: String): Boolean {
        return SpUtil.get(SpKey.TASK_RECEIVE_COIN_FINISH + taskId, false)
    }

    fun setReceiveCoinFinish(taskId: String) {
        SpUtil.put(SpKey.TASK_RECEIVE_COIN_FINISH + taskId, true)
    }

    fun getHitMoleGameFinish(taskId: String): Boolean {
        return SpUtil.get(SpKey.TASK_HIT_MOLE_GAME_FINISH + taskId, false)
    }

    fun setHitMoleGameFinish(taskId: String) {
        SpUtil.put(SpKey.TASK_HIT_MOLE_GAME_FINISH + taskId, true)
    }

    fun getReceiveCoinGameFinish(taskId: String): Boolean {
        return SpUtil.get(SpKey.TASK_RECEIVE_COIN_GAME_FINISH + taskId, false)
    }

    fun setReceiveCoinGameFinish(taskId: String) {
        SpUtil.put(SpKey.TASK_RECEIVE_COIN_GAME_FINISH + taskId, true)
    }

    // 签到相关功能

    /**
     * 执行签到操作
     * @return 是否签到成功（如果今天已签到则返回false）
     */
    fun signIn(cb: (refresh: Boolean) -> Unit) {
        // 检查今天是否已经签到
        if (isSignedToday()) {
            if (App.DEBUG) {
                log("今天已签到")
            }
//            toast(App.instance.getString(R.string.task_already_sign_today))
            cb.invoke(false)
            return
        }

        // 增加签到天数
        val currentCount = getSignCount()
        setSignCount(currentCount + 1)
        if (App.DEBUG) {
            log("签到第${getSignCount()}天")
        }

        // 记录今天的日期
        saveLastSignDate()
        cb.invoke(true)
    }

    /**
     * 检查今天是否已经签到
     */
    fun isSignedToday(): Boolean {
        val lastSignDate = SpUtil.get(SpKey.TASK_CHARGE_SIGN_DATE, "")
        val todayDate = getCurrentDateString()
        return lastSignDate == todayDate
    }

    /**
     * 获取当前日期字符串（格式：yyyyMMdd）
     */
    private fun getCurrentDateString(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d%02d%02d", year, month, day)
    }

    /**
     * 保存最后签到日期
     */
    private fun saveLastSignDate() {
        SpUtil.put(SpKey.TASK_CHARGE_SIGN_DATE, getCurrentDateString())
    }

    fun dotCharge(taskId: String) {
        val value = when (taskId) {
            TaskId.CHARGE_COLLECT_20 -> 1
            TaskId.CHARGE_COLLECT_100 -> 2
            TaskId.CHARGE_COLLECT_200 -> 3
            TaskId.CHARGE_COLLECT_400 -> 4
            TaskId.CHARGE_SIGN_1 -> 5
            TaskId.CHARGE_SIGN_2 -> 6
            TaskId.CHARGE_SIGN_3 -> 7
            TaskId.CHARGE_SIGN_4 -> 8
            TaskId.CHARGE_SIGN_5 -> 9
            TaskId.CHARGE_SIGN_6 -> 10
            TaskId.CHARGE_SIGN_7 -> 11
            TaskId.CHARGE_SIGN_8 -> 12
            TaskId.CHARGE_SIGN_9 -> 13
            TaskId.CHARGE_SIGN_10 -> 14
            else -> 0
        }
        if (value == 0) {
            return
        }
        SdkManager.dot("user_task_dian", mapOf("task_dian" to value))
    }

    fun dotHitMole(taskId: String) {
        val value = when (taskId) {
            TaskId.HIT_MOLE_50 -> 1
            TaskId.HIT_MOLE_300 -> 2
            TaskId.HIT_MOLE_1000 -> 3
            TaskId.HIT_MOLE_3000 -> 4
            TaskId.HIT_MOLE_10000 -> 5
            TaskId.HIT_MOLE_GAME_5 -> 6
            TaskId.HIT_MOLE_GAME_15 -> 7
            TaskId.HIT_MOLE_GAME_30 -> 8
            TaskId.HIT_MOLE_GAME_50 -> 9
            TaskId.HIT_MOLE_GAME_80 -> 10
            else -> 0
        }
        if (value == 0) {
            return
        }
        SdkManager.dot("user_task_shu", mapOf("task_shu" to value))
    }

    fun dotReceiveCoin(taskId: String) {
        val value = when (taskId) {
            TaskId.RECEIVE_COIN_200 -> 1
            TaskId.RECEIVE_COIN_1000 -> 2
            TaskId.RECEIVE_COIN_2500 -> 3
            TaskId.RECEIVE_COIN_5000 -> 4
            TaskId.RECEIVE_COIN_10000 -> 5
            TaskId.RECEIVE_COIN_GAME_5 -> 6
            TaskId.RECEIVE_COIN_GAME_15 -> 7
            TaskId.RECEIVE_COIN_GAME_30 -> 8
            TaskId.RECEIVE_COIN_GAME_50 -> 9
            TaskId.RECEIVE_COIN_GAME_80 -> 10
            else -> 0
        }
        if (value == 0) {
            return
        }
        SdkManager.dot("user_task_jinbi", mapOf("task_jinbi" to value))
    }


}