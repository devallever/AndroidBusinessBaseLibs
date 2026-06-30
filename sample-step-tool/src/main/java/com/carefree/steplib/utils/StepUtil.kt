package com.carefree.steplib.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import com.carefree.steplib.bean.StepBean
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.room.StepDb
import com.carefree.steplib.room.StepDbManager
import com.carefree.steplib.step.StepManager
import com.carefree.steplib.step.StepService
import java.util.Calendar
import java.util.Date

object StepUtil {
    lateinit var sApp: Application
    var isDebug: Boolean = false

    fun initStep(app: Application, debug: Boolean) {
        sApp = app
        isDebug = debug
        initStepDb(app)
    }

    fun startStepService() {
        //初始化计步模块
        StepManager.startTodayStepService(sApp)
        //开启计步Service，同时绑定Activity进行aidl通信
        val intent = Intent(sApp, StepService::class.java)
        try {
            sApp.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onDestroy() {
        try {
            sApp.stopService(Intent(sApp, StepService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val isUploadStep: Boolean
        /**
         * 是否上传步数，23:55:50~00:05:50分无法上传步数
         *
         * @return true可以上传，false不能上传
         */
        get() {
            val curDate = Date(System.currentTimeMillis())
            val mills2355 = DateUtils.getDateMillis(
                DateUtils.getCurrentDate() + " 23:55:50",
                "yyyy-MM-dd HH:mm:ss"
            )
            val date2355 = Date(mills2355)
            if (curDate.after(date2355)) {
                return false
            }
            val mills0005 = DateUtils.getDateMillis(
                DateUtils.getCurrentDate() + " 00:05:50",
                "yyyy-MM-dd HH:mm:ss"
            )
            val date0005 = Date(mills0005)
            return if (curDate.before(date0005)) {
                false
            } else true
        }
    val isUploadStepGoto: Boolean
        /**
         * 是否先上传步数在跳转，23:59:00~00:01:00分直接跳转不上传步数
         *
         * @return true上传后跳转，false直接跳转
         */
        get() {
            val curDate = Date(System.currentTimeMillis())
            val mills2355 = DateUtils.getDateMillis(
                DateUtils.getCurrentDate() + " 23:59:00",
                "yyyy-MM-dd HH:mm:ss"
            )
            val date2355 = Date(mills2355)
            if (curDate.after(date2355)) {
                return false
            }
            val mills0005 = DateUtils.getDateMillis(
                DateUtils.getCurrentDate() + " 00:01:00",
                "yyyy-MM-dd HH:mm:ss"
            )
            val date0005 = Date(mills0005)
            return if (curDate.before(date0005)) {
                false
            } else true
        }
    val isHealthTipsHide: Boolean
        /**
         * 23:30:00~00:05:00分隐藏tips
         *
         * @return true上传后跳转，false直接跳转
         */
        get() {
            val curDate = Date(System.currentTimeMillis())
            val mills2355 = DateUtils.getDateMillis(
                DateUtils.getCurrentDate() + " 23:30:00",
                "yyyy-MM-dd HH:mm:ss"
            )
            val date2355 = Date(mills2355)
            if (curDate.after(date2355)) {
                return false
            }
            val mills0005 = DateUtils.getDateMillis(
                DateUtils.getCurrentDate() + " 00:05:00",
                "yyyy-MM-dd HH:mm:ss"
            )
            val date0005 = Date(mills0005)
            return if (curDate.before(date0005)) {
                false
            } else true
        }

    @Volatile
    private var stepDB: StepDb? = null
    private fun initStepDb(context: Context): StepDb {
        return stepDB ?: synchronized(this) {
            val instance = StepDbManager.stepDB(context)
            stepDB = instance
            instance
        }
    }

    @Synchronized
    fun getStepAll():MutableList<StepBean>{
        if (stepDB ==null){
            return mutableListOf()
        }

        val stepBeans = stepDB!!.stepDao().getAll()
        if (stepBeans.isNullOrEmpty()){
            upStep(StepBean(0, DateUtils.getCurrentDate(),  System.currentTimeMillis(), 0, ConstStep.STEP_GOAL_DEF))
        }

        return stepDB!!.stepDao().getAll()!!.onEach {
            it.dayStep += it.dayStepBase
        }
    }

    @Synchronized
    fun getStepByFilter(startTime: Long, endTime: Long): MutableList<StepBean> {
        if (stepDB == null) {
            return mutableListOf()
        }
        return stepDB!!.stepDao().getStepByFilter(startTime, endTime)
    }

    @Synchronized
    fun getStepByDay(dayYmd: String): StepBean {
        if (stepDB == null) {
            return StepBean(-1, dayYmd, 0L, 0, 0)
        }
        return stepDB!!.stepDao().getStepByDate(dayYmd) ?: StepBean(-1, dayYmd, 0L, 0, 0)
    }

    @Synchronized
    fun upStep(stepBean: StepBean) {
        stepDB?.stepDao()?.upStep(stepBean)
    }

    @Synchronized
    fun upStepGoal(stepBean: StepBean) {
        stepDB?.stepDao()?.upStepGoal(stepBean)
    }

    private var calendar: Calendar? = null

    @Synchronized
    fun getStepByDay(startTime: Long, day: Int): MutableList<StepBean> {
        // 没有的数据需要填充
        val list: MutableList<StepBean> = mutableListOf()
        if (calendar == null) {
            calendar = Calendar.getInstance()
        }
        for (i in 0 until day) {
            calendar!!.timeInMillis = startTime
            calendar!!.add(Calendar.DATE, i)
            val stepBean = getStepByDay(DateUtils.dateFormat(calendar!!.timeInMillis, DateUtils.DATE_FORMAT_YMD))
            if (stepBean.time == 0L) {
                stepBean.time = calendar!!.timeInMillis
            }
            stepBean.dayStep += stepBean.dayStepBase
            list.add(stepBean)
        }
        return list
    }
}