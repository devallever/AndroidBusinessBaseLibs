package com.carefree.steplib.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Binder
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.carefree.steplib.bean.StepBean
import com.carefree.steplib.common.StepConstants
import com.carefree.steplib.core.StepChangeListener
import com.carefree.steplib.core.StepCounterImpl
import com.carefree.steplib.core.StepDetectorImpl
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.utils.DateUtils
import com.carefree.steplib.utils.StepTracker
import com.carefree.steplib.utils.WakeLockUtils
import com.carefree.steplib.utils.WeakHandler
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * 计步跟踪服务
 */
class StepTrackingService : Service(), WeakHandler.IHandler {
    private var sensorManager: SensorManager? = null
    private var stepDetector: StepDetectorImpl? = null
    private var stepCounter: StepCounterImpl? = null
    private var isSeparate = false
    private var isBoot = false
    private var dbSaveCounter = 0
    private val handler = WeakHandler(this)
    private var sensorType = 0

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): StepTrackingService {
            return this@StepTrackingService
        }
    }

    override fun handleMsg(msg: Message) {
        when (msg.what) {
            MSG_SAVE_STEP -> {
                // 走路停止保存数据库
                dbSaveCounter = 0
                saveStepToDatabase(currentStepCount)
            }
            MSG_REFRESH_NOTIFICATION -> {
                // 刷新通知栏
                updateStepCount(currentStepCount)
                
                // 移除之前的消息并延迟发送新消息
                handler.removeMessages(MSG_REFRESH_NOTIFICATION)
                handler.sendEmptyMessageDelayed(
                    MSG_REFRESH_NOTIFICATION,
                    StepConstants.DELAY_REFRESH_NOTIFY.toLong()
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        
        // 创建通知通道（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                StepConstants.NOTIFICATION_CHANNEL_ID,
                StepConstants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
            
            // 创建前台服务通知
            val notification = NotificationCompat.Builder(this, StepConstants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(StepTracker.notificationConfig.stepNotificationTitle)
                .setContentText(StepTracker.notificationConfig.stepNotificationMessage)
                .setSmallIcon(android.R.drawable.ic_menu_directions)
                .build()
            
            // 启动前台服务
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 获取意图中的参数
        isSeparate = intent?.getBooleanExtra(StepConstants.INTENT_EXTRA_SEPARATE, false)?: false
        isBoot = intent?.getBooleanExtra(StepConstants.INTENT_EXTRA_BOOT, false)?: false
        
        // 初始化步数（如果有）
        val initialStep = intent?.getStringExtra(StepConstants.INTENT_EXTRA_STEP_INIT)
        if (!TextUtils.isEmpty(initialStep)) {
            try {
                setInitialSteps(initialStep!!.toInt())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        
        // 重置数据库保存计数器
        dbSaveCounter = 0
        
        // 注册传感器
        registerStepSensors()
        
        // 启动通知栏刷新
        startNotificationRefresh()
        
        // 设置服务为粘性，确保被杀死后能重启
        return START_STICKY
    }

    private fun registerStepSensors() {
        // Android 10以上需要活动识别权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED && hasStepCounterSensor
            ) {
                registerStepCounterSensor()
            } else {
                registerAccelerometerSensor()
            }
        } else if (hasStepCounterSensor) {
            registerStepCounterSensor()
        } else {
            registerAccelerometerSensor()
        }
    }

    private fun registerStepCounterSensor() {
        if (stepCounter != null) {
            WakeLockUtils.getLock(this)
            currentStepCount = stepCounter!!.getCurrentStep
            return
        }
        
        val counterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
        sensorType = SENSOR_TYPE_STEP_COUNTER
        
        stepCounter = StepCounterImpl(
            applicationContext,
            stepChangeListener,
            isSeparate,
            isBoot
        )
        
        currentStepCount = stepCounter!!.getCurrentStep
        val registerSuccess = sensorManager?.registerListener(
            stepCounter,
            counterSensor,
            StepConstants.SENSOR_SAMPLING_PERIOD
        ) ?: false
    }

    private fun registerAccelerometerSensor() {
        if (stepDetector != null) {
            WakeLockUtils.getLock(this)
            currentStepCount = stepDetector!!.getCurrentStep
            return
        }
        
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorType = SENSOR_TYPE_ACCELEROMETER
        
        stepDetector = StepDetectorImpl(
            applicationContext,
            stepChangeListener
        )
        
        currentStepCount = stepDetector!!.getCurrentStep
        val registerSuccess = sensorManager?.registerListener(
            stepDetector,
            accelerometer,
            StepConstants.SENSOR_SAMPLING_PERIOD
        ) ?: false
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销传感器监听器并清理资源
        unregisterSensors()
    }

    private fun unregisterSensors() {
        stepDetector?.let {
            sensorManager?.unregisterListener(it)
        }
        
        stepCounter?.let {
            sensorManager?.unregisterListener(it)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // 返回服务的Binder实例
        return binder
    }
    
    override fun onUnbind(intent: Intent): Boolean {
        // 继续刷新通知栏
        handler.removeMessages(MSG_REFRESH_NOTIFICATION)
        handler.sendEmptyMessageDelayed(
            MSG_REFRESH_NOTIFICATION,
            StepConstants.DELAY_REFRESH_NOTIFY.toLong()
        )
        
        return super.onUnbind(intent)
    }

    private fun updateStepCount(stepCount: Int) {
        currentStepCount = stepCount
        scheduleStepSave(stepCount)
    }

    private fun scheduleStepSave(stepCount: Int) {
        // 取消之前的保存任务
        handler.removeMessages(MSG_SAVE_STEP)
        // 延迟发送保存消息
        handler.sendEmptyMessageDelayed(MSG_SAVE_STEP, StepConstants.DELAY_SAVE_STEP.toLong())
        
        // 如果步数没有变化，不保存
        if (stepCount == lastSavedStepCount) {
            return
        }
        
        lastSavedStepCount = stepCount
        saveStepToDatabase(stepCount)
    }

    private fun saveStepToDatabase(stepCount: Int) {
        val stepBean = createStepBean(stepCount)
        StepTracker.updateStep(stepBean)
        
        // 发送步数更新事件
        LiveEventBus.get<Int>(ConstStep.STEP_EVENT)
            .postAcrossProcess(stepCount)
    }

    private fun createStepBean(stepCount: Int): StepBean {
        return if (sensorType == SENSOR_TYPE_ACCELEROMETER) {
            StepBean(
                0,
                getCurrentDate,
                System.currentTimeMillis(),
                -1,
                StepConstants.DEFAULT_STEP_GOAL,
                ArrayList(),
                stepCount
            )
        } else {
            StepBean(
                0,
                getCurrentDate,
                System.currentTimeMillis(),
                stepCount,
                StepConstants.DEFAULT_STEP_GOAL,
                ArrayList(),
                -1
            )
        }
    }

    private fun startNotificationRefresh() {
        handler.removeMessages(MSG_REFRESH_NOTIFICATION)
        handler.sendEmptyMessageDelayed(
            MSG_REFRESH_NOTIFICATION,
            StepConstants.DELAY_REFRESH_NOTIFY.toLong()
        )
    }

    private fun setInitialSteps(steps: Int) {
        if (stepDetector != null) {
            stepDetector!!.setSteps(steps)
        }
    }

    private val hasStepCounterSensor: Boolean
        get() = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null

    private val getCurrentDate: String
        get() = DateUtils.getCurrentDate()

    private val stepChangeListener = object : StepChangeListener {
        override fun onStepChanged(step: Int) {
            currentStepCount = step
            
            // 发送实时步数更新事件给界面
            LiveEventBus.get<Int>(ConstStep.STEP_EVENT)
                .postAcrossProcess(step)
        }

        override fun onStepClean() {
            currentStepCount = 0
            LiveEventBus.get<Boolean>(ConstStep.STEP_EVENT_CHANGE)
                .postAcrossProcess(true)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val MSG_SAVE_STEP = 1
        private const val MSG_REFRESH_NOTIFICATION = 2
        private const val SENSOR_TYPE_ACCELEROMETER = 0
        private const val SENSOR_TYPE_STEP_COUNTER = 1
        
        var currentStepCount = 0
        private var lastSavedStepCount = -1
    }
}