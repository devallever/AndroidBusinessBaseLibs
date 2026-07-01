package com.carefree.steplib.step

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.BatteryManager
import android.os.PowerManager
import android.os.SystemClock
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.utils.DateUtils
import com.carefree.steplib.utils.Mkv
import com.carefree.steplib.utils.WakeLockUtils
import com.carefree.steplib.utils.logD

/**
 * Sensor.TYPE_STEP_COUNTER
 * 计步传感器计算当天步数，不需要后台Service
 * Created by jiahongfei on 2017/6/30.
 */
internal class StepCounter(
    private val mContext: Context,
    stepCounterListener: StepCounterListener?,
    separate: Boolean,
    boot: Boolean
) : SensorEventListener {
    private var sOffsetStep = 0
    private var sCurrStep = 0
    private var mTodayDate: String
    private var mCleanStep = true
    private var mShutdown = false

    /**
     * 用来标识对象第一次创建，
     */
    private var mCounterStepReset = true

    private val mStepCounterListener: StepCounterListener?

    private var mSeparate = false
    private var mBoot = false
    private var mBatInfoReceiver: BroadcastReceiver? = null

    init {
        this.mSeparate = separate
        this.mBoot = boot
        this.mStepCounterListener = stepCounterListener

        WakeLockUtils.getLock(mContext)

        sCurrStep = Mkv.getInt(ConstStep.CURR_STEP)
        mCleanStep = Mkv.getBool(ConstStep.CLEAN_STEP)
        mTodayDate = Mkv.getString(ConstStep.STEP_TODAY)
        sOffsetStep = Mkv.getInt(ConstStep.STEP_OFFSET)
        mShutdown = Mkv.getBool(ConstStep.SHUTDOWN)
        //开机启动监听到，一定是关机开机了
        val isShutdown = shutdownBySystemRunningTime()
        if (mBoot || isShutdown) {
            mShutdown = true
            Mkv.put(ConstStep.SHUTDOWN, true)
        }
        dateChangeCleanStep()

        initBroadcastReceiver()

        updateStepCounter()
    }

    private fun initBroadcastReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        mBatInfoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (Intent.ACTION_TIME_TICK == intent.action || Intent.ACTION_TIME_CHANGED == intent.action) {
                    //service存活做0点分隔
                    dateChangeCleanStep()
                }
            }
        }
        mContext.registerReceiver(mBatInfoReceiver, filter)
    }

    fun unregisterReceiver() {
        if (mBatInfoReceiver != null) {
            mContext.unregisterReceiver(mBatInfoReceiver)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val counterStep = event.values[0].toInt()

            if (mCleanStep) {
                //只有传感器回调才会记录当前传感器步数，然后对当天步数进行清零，所以步数会少，少的步数等于传感器启动需要的步数，假如传感器需要10步进行启动，那么就少10步
                cleanStep(counterStep)
            } else {
                //处理关机启动
                if (mShutdown || shutdownByCounterStep(counterStep)) {
                    shutdown(counterStep)
                }
            }
            sCurrStep = counterStep - sOffsetStep

            logD(
                "TodayStepCounter",
                "---counterStep==$counterStep,sCurrStep==$sCurrStep,sOffsetStep==$sOffsetStep"
            )
            if (sCurrStep < 0) {
                //容错处理，无论任何原因步数不能小于0，如果小于0，直接清零
                cleanStep(counterStep)
            }
            Mkv.put(ConstStep.CURR_STEP, sCurrStep)
            Mkv.put(ConstStep.ELAPSED_REALTIMEl, SystemClock.elapsedRealtime())
            Mkv.put(ConstStep.LAST_SENSOR_TIME, counterStep)

            updateStepCounter()
        }
    }

    private fun cleanStep(counterStep: Int) {
        //清除步数，步数归零，优先级最高
        sCurrStep = 0
        sOffsetStep = counterStep
        Mkv.put(ConstStep.STEP_OFFSET, sOffsetStep)

        mCleanStep = false
        Mkv.put(ConstStep.CLEAN_STEP, false)
    }

    private fun shutdown(counterStep: Int) {
        val tmpCurrStep = Mkv.getInt(ConstStep.CURR_STEP)
        //重新设置offset
        sOffsetStep = counterStep - tmpCurrStep
        // 只有在当天进行过关机，才会进入到这，直接置反??
//        sOffsetStep = -tmpCurrStep;
        Mkv.put(ConstStep.STEP_OFFSET, sOffsetStep)

        mShutdown = false
        Mkv.put(ConstStep.SHUTDOWN, false)
    }

    private fun shutdownByCounterStep(counterStep: Int): Boolean {
        if (mCounterStepReset) {
            //只判断一次
            mCounterStepReset = false
            if (counterStep < Mkv.getInt(ConstStep.LAST_SENSOR_TIME)) {
                //当前传感器步数小于上次传感器步数肯定是重新启动了，只是用来增加精度不是绝对的
//                Logger.e(TAG, "当前传感器步数小于上次传感器步数肯定是重新启动了，只是用来增加精度不是绝对的");
                return true
            }
        }
        return false
    }

    private fun shutdownBySystemRunningTime(): Boolean {
        if (Mkv.getLong(ConstStep.ELAPSED_REALTIMEl) > SystemClock.elapsedRealtime()) {
            //上次运行的时间大于当前运行时间判断为重启，只是增加精度，极端情况下连续重启，会判断不出来
//            Logger.e(TAG, "上次运行的时间大于当前运行时间判断为重启，只是增加精度，极端情况下连续重启，会判断不出来");
            return true
        }
        return false
    }

    @Synchronized
    private fun dateChangeCleanStep() {
        //时间改变了清零，或者0点分隔回调

        if (todayDate != mTodayDate || mSeparate) {
            WakeLockUtils.getLock(mContext)

            mCleanStep = true
            Mkv.put(ConstStep.CLEAN_STEP, true)

            mTodayDate = todayDate
            Mkv.put(ConstStep.STEP_TODAY, mTodayDate)

            mShutdown = false
            Mkv.put(ConstStep.SHUTDOWN, false)

            mBoot = false

            mSeparate = false

            sCurrStep = 0
            Mkv.put(ConstStep.CURR_STEP, sCurrStep)

            mStepCounterListener?.stepClean()
        }
    }

    private val todayDate: String
        get() = DateUtils.getCurrentDate()

    private fun updateStepCounter() {
        //每次回调都判断一下是否跨天
        dateChangeCleanStep()

        mStepCounterListener?.stepChange(sCurrStep)
    }

    val currentStep: Int
        get() {
            sCurrStep = Mkv.getInt(ConstStep.CURR_STEP)
            return sCurrStep
        }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    private val battery: Int
        get() {
            val batteryManager =
                mContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val battery: Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            return battery
        }
    private val screenState: Boolean
        get() {
            val pm = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isScreenOn
        }

    companion object {
        private const val TAG = "TodayStepCounter"
    }
}
