package com.example.charge.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.charge.task.TaskHelper

class TaskViewModel : ViewModel() {

    //充电收集泡泡次数
//    val chargeCollectCount = MutableLiveData<Int>()
//    //充电签到次数
//    val chargeSignCount = MutableLiveData<Int>()
    val hitMoleCount = MutableLiveData<Int>()//打地鼠的次数
    val receiveCoinCount = MutableLiveData<Int>() //接到金币的次数
    val hitMoleGameCount = MutableLiveData<Int>() //打地鼠游戏次数
    val receiveCoinGameCount = MutableLiveData<Int>() //接金币游戏次数
    //
    val showTaskDot = MutableLiveData<Boolean>()//首页显示任务点 全部

    init {
        hitMoleCount.value = TaskHelper.getHitMoleCount()
        hitMoleGameCount.value = TaskHelper.getHitMoleGameCount()
        receiveCoinCount.value = TaskHelper.getReceiveCoinCount()
        receiveCoinGameCount.value = TaskHelper.getReceiveCoinGameCount()
//        chargeCollectCount.value = TaskHelper.getCollectCount()
//        chargeSignCount.value = TaskHelper.getSignCount()
        showTaskDot.value = false

        hitMoleCount.observeForever {
            TaskHelper.setHitMoleCount(it)
        }
        receiveCoinCount.observeForever {
            TaskHelper.setReceiveCoinCount(it)
        }
        hitMoleGameCount.observeForever {
            TaskHelper.setHitMoleGameCount(it)
        }
        receiveCoinGameCount.observeForever {
            TaskHelper.setReceiveCoinGameCount(it)
        }
    }
}