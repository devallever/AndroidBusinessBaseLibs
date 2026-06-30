package com.carefree.steplib.step

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.carefree.steplib.bean.StepBean
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.utils.DateUtils
import com.carefree.steplib.utils.StepUtil
import com.carefree.steplib.utils.WakeLockUtils
import com.carefree.steplib.utils.WeakHandler
import com.carefree.steplib.utils.logD
import com.jeremyliao.liveeventbus.LiveEventBus

class StepService : Service(), WeakHandler.IHandler {
    private var mSensorManager: SensorManager? = null

    /**
     * Sensor.TYPE_ACCELEROMETER
     * 加速度传感器计算当天步数，需要保持后台Service
     */
    private var mStepDetector: StepDetector? = null

    /**
     * Sensor.TYPE_STEP_COUNTER
     * 计步传感器计算当天步数，不需要后台Service
     */
    private var mStepCounter: StepCounter? = null

    private var mSeparate = false
    private var mBoot = false

    /**
     * 保存数据库计数器
     */
    private var mDbSaveCount = 0

    /**
     * 数据库
     */
    //private StepDb mTodayStepDBHelper;
    private val sHandler = WeakHandler(this)
    private var sensorType = 0

    private val binder: LocalBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): StepService {
            return this@StepService
        }
    }

    override fun handleMsg(msg: Message) {
        when (msg.what) {
            HANDLER_WHAT_SAVE_STEP -> {
                //走路停止保存数据库
                mDbSaveCount = 0

                saveDb(currentTimeSportStep)
            }

            HANDLER_WHAT_REFRESH_NOTIFY_STEP -> {
                //刷新通知栏
                updateTodayStep(currentTimeSportStep)

                sHandler.removeMessages(HANDLER_WHAT_REFRESH_NOTIFY_STEP)
                sHandler.sendEmptyMessageDelayed(
                    HANDLER_WHAT_REFRESH_NOTIFY_STEP,
                    REFRESH_NOTIFY_STEP_DURATION.toLong()
                )
            }

            else -> {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //initNotification(CURRENT_STEP);
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                STEP_CHANNEL_ID,
                "step_lib",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )

            val notification: Notification = NotificationCompat.Builder(this, STEP_CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()

            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mSeparate = intent.getBooleanExtra(INTENT_NAME_0_SEPARATE, false)
        mBoot = intent.getBooleanExtra(INTENT_NAME_BOOT, false)
        val setStep = intent.getStringExtra(INTENT_STEP_INIT)
        if (!TextUtils.isEmpty(setStep)) {
            try {
                setSteps(setStep!!.toInt())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }

        mDbSaveCount = 0

        //注册传感器
        startStepDetector()

        sHandler.removeMessages(HANDLER_WHAT_REFRESH_NOTIFY_STEP)
        sHandler.sendEmptyMessageDelayed(
            HANDLER_WHAT_REFRESH_NOTIFY_STEP,
            REFRESH_NOTIFY_STEP_DURATION.toLong()
        )

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        sHandler.removeMessages(HANDLER_WHAT_REFRESH_NOTIFY_STEP)
        sHandler.sendEmptyMessageDelayed(
            HANDLER_WHAT_REFRESH_NOTIFY_STEP,
            REFRESH_NOTIFY_STEP_DURATION.toLong()
        )

        return binder
    }

    private fun startStepDetector() {
        //android4.4以后如果有stepcounter可以使用计步传感器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED && stepCounter
            ) {
                addStepCounterListener()
            } else {
                addBasePedoListener()
            }
        } else if (stepCounter) {
            addStepCounterListener()
        } else {
            addBasePedoListener()
        }
    }

    /**
     * 计步传感器
     */
    private fun addStepCounterListener() {
        if (null != mStepCounter) {
            WakeLockUtils.getLock(this)
            currentTimeSportStep = mStepCounter!!.currentStep
            return
        }
        val countSensor: Sensor =
            mSensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
        sensorType = 1
        mStepCounter =
            StepCounter(applicationContext, mStepCounterListener, mSeparate, mBoot)
        currentTimeSportStep = mStepCounter!!.currentStep
        val registerSuccess: Boolean =
            mSensorManager?.registerListener(mStepCounter, countSensor, SAMPLING_PERIOD_US) ?: false
        logD("TodayStepCounter", "addStepCounterListener == $registerSuccess")
    }

    /**
     * 陀螺仪计步
     */
    private fun addBasePedoListener() {
        if (null != mStepDetector) {
            WakeLockUtils.getLock(this)
            currentTimeSportStep = mStepDetector!!.currentStep
            return
        }
        //没有计步器的时候开启定时器保存数据
        val sensor: Sensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorType = 0
        mStepDetector = StepDetector(this, mStepCounterListener)
        currentTimeSportStep = mStepDetector!!.currentStep
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        val registerSuccess: Boolean =
            mSensorManager?.registerListener(mStepDetector, sensor, SAMPLING_PERIOD_US) ?: false
        logD("TodayStepCounter", "addBasePedoListener == $registerSuccess")
    }

    override fun onDestroy() {
        super.onDestroy()
        mStepDetector?.let {
            it.unregisterReceiver()
            mSensorManager?.unregisterListener(it)
        }

        mStepCounter?.let {
            it.unregisterReceiver()
            mSensorManager?.unregisterListener(it)
        }
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    /**
     * 步数每次回调的方法
     *
     * @param currentStep
     */
    private fun updateTodayStep(currentStep: Int) {
        //Log.e(TAG,"currentStep = "+currentStep);
        currentTimeSportStep = currentStep
        saveStep(currentStep)
    }

    private var saveStep = -1
    private fun saveStep(currentStep: Int) {
        sHandler.removeMessages(HANDLER_WHAT_SAVE_STEP)
        sHandler.sendEmptyMessageDelayed(HANDLER_WHAT_SAVE_STEP, LAST_SAVE_STEP_DURATION.toLong())

        /*if (DB_SAVE_COUNTER > mDbSaveCount) {
            mDbSaveCount++;
            return;
        }
        mDbSaveCount = 0;*/
        if (currentStep == saveStep) {
            return
        }

        saveStep = currentStep
        saveDb(currentStep)
    }

    /**
     * @param currentStep
     */
    private fun saveDb(currentStep: Int) {
        val stepEntity: StepBean
        if (sensorType == 0) {
            stepEntity = StepBean(
                0,
                todayDate,
                System.currentTimeMillis(),
                -1,
                ConstStep.STEP_GOAL_DEF,
                ArrayList(),
                currentStep
            )
        } else {
            stepEntity = StepBean(
                0,
                todayDate,
                System.currentTimeMillis(),
                currentStep,
                ConstStep.STEP_GOAL_DEF,
                ArrayList(),
                -1
            )
        }

        StepUtil.upStep(stepEntity)
        logD("TodayStepCounter", "saveDb stepEntity == $stepEntity")
        LiveEventBus.get<Int>(ConstStep.STEP_EVENT).postAcrossProcess(currentStep)
    }

    private fun cleanDb() {
        mDbSaveCount = 0

        //if (null != mTodayStepDBHelper) {
        //保存多天的步数
        //mTodayStepDBHelper.deleteTable();
        //mTodayStepDBHelper.createTable();
        //}
    }

    private val todayDate: String
        get() = DateUtils.getCurrentDate()

    private val stepCounter: Boolean
        get() {
            mSensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return false
            return true
        }

    private val mStepCounterListener: StepCounterListener = object : StepCounterListener {
        override fun stepChange(step: Int) {
            //if (StepUtil.isUploadStep) {
                currentTimeSportStep = step
            //}
        }

        override fun stepClean() {
            currentTimeSportStep = 0
            //cleanDb();
            LiveEventBus.get<Boolean>(ConstStep.STEP_EVENT_CHANGE).postAcrossProcess(true)
        }
    }

    /**
     * 设置步数初始值，目前只支持设置用加速度传感器进行计步
     *
     * @param steps
     */
    private fun setSteps(steps: Int) {
        if (null != mStepDetector) {
            mStepDetector!!.currentStep = steps
        }
    }

    companion object {
        private const val TAG = "TodayStepService"

        private const val STEP_CHANNEL_ID = "stepChannelId"

        /**
         * 传感器刷新频率
         */
        //private static final int SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_FASTEST;
        private val SAMPLING_PERIOD_US: Int = SensorManager.SENSOR_DELAY_GAME

        /**
         * 运动停止保存步数
         */
        private const val HANDLER_WHAT_SAVE_STEP = 0

        /**
         * 刷新通知栏步数
         */
        private const val HANDLER_WHAT_REFRESH_NOTIFY_STEP = 2


        /**
         * 如果走路如果停止，10秒钟后保存数据库
         */
        private const val LAST_SAVE_STEP_DURATION = 10 * 1000

        /**
         * 刷新通知栏步数，3s一次
         */
        private const val REFRESH_NOTIFY_STEP_DURATION = 3 * 1000

        /**
         * 点击通知栏广播requestCode
         */
        private const val BROADCAST_REQUEST_CODE = 100

        const val INTENT_NAME_0_SEPARATE: String = "intent_name_0_separate"
        const val INTENT_NAME_BOOT: String = "intent_name_boot"
        const val INTENT_STEP_INIT: String = "intent_step_init"
        const val INTENT_STEP_GOAL: String = "intent_step_goal"

        /**
         * 当前步数
         */
        var currentTimeSportStep: Int = 0

        /*fun getReceiver(context: Context): String? {
            try {
                val packageInfo: PackageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_RECEIVERS
                )
                val activityInfos: Array<ActivityInfo>? = packageInfo.receivers
                if (!activityInfos.isNullOrEmpty()) {
                    for (i in activityInfos.indices) {
                        val receiverName: String = activityInfos[i].name
                        var superClazz = Class.forName(receiverName).superclass
                        var count = 1
                        while (null != superClazz) {
                            if (superClazz.name == "java.lang.Object") {
                                break
                            }
                            if (superClazz.name == BaseClickBroadcast::class.java.name) {
                                return receiverName
                            }
                            if (count > 20) {
                                //用来做容错，如果20个基类还不到Object直接跳出防止while死循环
                                break
                            }
                            count++
                            superClazz = superClazz.superclass
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }*/
    }
}
