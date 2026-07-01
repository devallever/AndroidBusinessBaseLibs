package com.carefree.steplib.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.carefree.steplib.bean.StepBean
import com.carefree.steplib.common.StepConstants
import com.carefree.steplib.room.StepDb
import com.carefree.steplib.room.StepDbManager
import com.carefree.steplib.service.StepTrackingService
import java.util.Calendar
import java.util.Date

/**
 * 计步器管理工具类
 */
object StepTracker {
    private lateinit var applicationContext: Application
    var isDebugMode: Boolean = false
    
    @Volatile
    private var stepDatabase: StepDb? = null
    
    /**
     * 获取应用包名
     */
    val packageName: String
        get() = applicationContext.packageName

    lateinit var notificationConfig: Config

    /**
     * 初始化计步模块
     * @param app 应用上下文
     * @param debug 是否为调试模式
     */
    fun initialize(app: Application, notificationCfg: Config,  debug: Boolean) {
        this.notificationConfig = notificationCfg
        applicationContext = app
        isDebugMode = debug
        initializeDatabase(app)
    }

    /**
     * 启动计步服务
     */
    fun startTrackingService() {
        // 检查是否有必要的权限
        if (!hasRequiredPermissions()) {
            if (isDebugMode) {
                android.util.Log.w("StepTracker", "缺少必要的权限，无法启动计步服务")
            }
            return
        }
        
        // 启动计步服务
        val intent = Intent(applicationContext, StepTrackingService::class.java)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查是否拥有必要的权限
     */
    private fun hasRequiredPermissions(): Boolean {
        // Android 10及以上需要活动识别权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val activityRecognitionPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            )
            
            if (activityRecognitionPermission != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        
        // Android 13及以上需要通知权限
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
//            val notificationPermission = ContextCompat.checkSelfPermission(
//                applicationContext,
//                android.Manifest.permission.POST_NOTIFICATIONS
//            )
//
//            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
//                return false
//            }
//        }
        
        return true
    }

    /**
     * 停止计步服务
     */
    fun stopTrackingService() {
        try {
            applicationContext.stopService(Intent(applicationContext, StepTrackingService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 是否可以上传步数（避免在0点附近上传导致数据不准确）
     * @return true 可以上传，false 不能上传
     */
    val canUploadStep: Boolean
        get() {
            val currentTime = Date(System.currentTimeMillis())
            val time2355 = getTimeMillisAt(23, 55, 50)
            val time0005 = getTimeMillisAt(0, 5, 50)
            
            // 23:55:50 ~ 00:05:50 之间不能上传
            return !(currentTime.after(Date(time2355)) || currentTime.before(Date(time0005)))
        }

    /**
     * 是否需要先上传步数再跳转
     * @return true 上传后跳转，false 直接跳转
     */
    val shouldUploadBeforeNavigation: Boolean
        get() {
            val currentTime = Date(System.currentTimeMillis())
            val time2359 = getTimeMillisAt(23, 59, 0)
            val time0001 = getTimeMillisAt(0, 1, 0)
            
            // 23:59:00 ~ 00:01:00 之间直接跳转不上传
            return !(currentTime.after(Date(time2359)) || currentTime.before(Date(time0001)))
        }

    /**
     * 是否应该隐藏健康提示
     * @return true 隐藏提示，false 显示提示
     */
    val shouldHideHealthTips: Boolean
        get() {
            val currentTime = Date(System.currentTimeMillis())
            val time2330 = getTimeMillisAt(23, 30, 0)
            val time0005 = getTimeMillisAt(0, 5, 0)
            
            // 23:30:00 ~ 00:05:00 之间隐藏提示
            return currentTime.after(Date(time2330)) || currentTime.before(Date(time0005))
        }

    /**
     * 获取当天指定时间的毫秒数
     */
    private fun getTimeMillisAt(hour: Int, minute: Int, second: Int): Long {
        val timeString = "${DateUtils.getCurrentDate()} $hour:$minute:$second"
        return DateUtils.getDateMillis(timeString, "yyyy-MM-dd HH:mm:ss")
    }

    /**
     * 初始化数据库
     */
    private fun initializeDatabase(context: Context): StepDb {
        return stepDatabase ?: synchronized(this) {
            val instance = StepDbManager.stepDB(context)
            stepDatabase = instance
            instance
        }
    }

    /**
     * 获取所有步数数据
     * @return 步数数据列表
     */
    @Synchronized
    fun getAllStepData(): MutableList<StepBean> {
        if (stepDatabase == null) {
            return mutableListOf()
        }
        
        val stepData = stepDatabase!!.stepDao().getAll()
        if (stepData.isNullOrEmpty()) {
            // 如果没有数据，创建今天的初始数据
            val todayStep = StepBean(
                0,
                DateUtils.getCurrentDate(),
                System.currentTimeMillis(),
                0,
                StepConstants.DEFAULT_STEP_GOAL
            )
            updateStep(todayStep)
        }
        
        // 计算总步数（基础步数+当天步数）
        return stepData!!.map { 
            it.copy(dayStep = it.dayStep + it.dayStepBase)
        }.toMutableList()
    }

    /**
     * 根据时间范围获取步数数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 步数数据列表
     */
    @Synchronized
    fun getStepDataByTimeRange(startTime: Long, endTime: Long): MutableList<StepBean> {
        if (stepDatabase == null) {
            return mutableListOf()
        }
        return stepDatabase!!.stepDao().getStepByFilter(startTime, endTime)
    }

    /**
     * 根据日期获取步数数据
     * @param dateString 日期字符串（yyyy-MM-dd格式）
     * @return 步数数据
     */
    @Synchronized
    fun getStepDataByDate(dateString: String): StepBean {
        if (stepDatabase == null) {
            return StepBean(-1, dateString, 0L, 0, 0)
        }
        return stepDatabase!!.stepDao().getStepByDate(dateString) 
            ?: StepBean(-1, dateString, 0L, 0, 0)
    }

    /**
     * 更新步数数据
     * @param stepBean 步数数据
     */
    @Synchronized
    fun updateStep(stepBean: StepBean) {
        stepDatabase?.stepDao()?.upStep(stepBean)
    }

    /**
     * 更新步数目标
     * @param stepBean 包含新目标的步数数据
     */
    @Synchronized
    fun updateStepGoal(stepBean: StepBean) {
        stepDatabase?.stepDao()?.upStepGoal(stepBean)
    }

    /**
     * 获取指定日期范围内的步数数据
     * @param startTime 开始时间
     * @param days 天数
     * @return 步数数据列表
     */
    @Synchronized
    fun getStepDataByDays(startTime: Long, days: Int): MutableList<StepBean> {
        val stepDataList = mutableListOf<StepBean>()
        val calendar = Calendar.getInstance()
        
        for (i in 0 until days) {
            calendar.timeInMillis = startTime
            calendar.add(Calendar.DATE, i)
            
            val dateString = DateUtils.dateFormat(calendar.timeInMillis, DateUtils.DATE_FORMAT_YMD)
            val stepBean = getStepDataByDate(dateString)
            
            // 确保时间戳正确
            if (stepBean.time == 0L) {
                stepBean.time = calendar.timeInMillis
            }
            
            // 计算总步数
            stepBean.dayStep += stepBean.dayStepBase
            stepDataList.add(stepBean)
        }
        
        return stepDataList
    }

    data class Config( var stepNotificationTitle: String = "",
                       var stepNotificationMessage: String = "") {

    }
}