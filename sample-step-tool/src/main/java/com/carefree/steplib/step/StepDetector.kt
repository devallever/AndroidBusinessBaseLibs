package com.carefree.steplib.step

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.utils.DateUtils
import com.carefree.steplib.utils.Mkv
import com.carefree.steplib.utils.WakeLockUtils
import kotlin.math.sqrt

/**
 * Sensor.TYPE_ACCELEROMETER
 * 加速度传感器计算当天步数，需要保持后台Service
 */
class StepDetector(
    private val mContext: Context,
    private val mStepCounterListener: StepCounterListener?
) : SensorEventListener {

    //存放三轴数据
    private var oriValues: FloatArray = FloatArray(3)
    private val valueNum: Int = 4

    //用于存放计算阈值的波峰波谷差值
    private var tempValue: FloatArray = FloatArray(valueNum)
    private var tempCount: Int = 0

    //是否上升的标志位
    private var isDirectionUp: Boolean = false

    //持续上升次数
    private var continueUpCount: Int = 0

    //上一点的持续上升的次数，为了记录波峰的上升次数
    private var continueUpFormerCount: Int = 0

    //上一点的状态，上升还是下降
    private var lastStatus: Boolean = false

    //波峰值
    private var peakOfWave: Float = 0f

    //波谷值
    private var valleyOfWave: Float = 0f

    //此次波峰的时间
    private var timeOfThisPeak: Long = 0

    //上次波峰的时间
    private var timeOfLastPeak: Long = 0

    //当前的时间
    private var timeOfNow: Long = 0

    //当前传感器的值
    private var gravityNew: Float = 0f

    //上次传感器的值
    private var gravityOld: Float = 0f

    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    private val InitialValue: Float = 1.3.toFloat()

    //初始阈值
    private var ThreadValue: Float = 2.0.toFloat()

    //波峰波谷时间差
    private var TimeInterval: Int = 250

    private var count = 0
    private var mCount = 0
    private var timeOfLastPeak1: Long = 0
    private var timeOfThisPeak1: Long = 0
    private var mTodayDate: String

    private var mBatInfoReceiver: BroadcastReceiver? = null

    init {
        WakeLockUtils.getLock(mContext)
        mCount = Mkv.getInt(ConstStep.CURR_STEP)
        mTodayDate = Mkv.getString(ConstStep.STEP_TODAY)
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
        if (null != mBatInfoReceiver) {
            mContext.unregisterReceiver(mBatInfoReceiver)
        }
    }

    @Synchronized
    private fun dateChangeCleanStep() {
        //时间改变了清零，或者0点分隔回调
        if (todayDate != mTodayDate) {
            WakeLockUtils.getLock(mContext)

            mCount = 0
            Mkv.put(ConstStep.CURR_STEP, mCount)

            mTodayDate = todayDate
            Mkv.put(ConstStep.STEP_TODAY, mTodayDate)
            setSteps(0)
            mStepCounterListener?.stepClean()
        }
    }

    private val todayDate: String
        get() = DateUtils.getCurrentDate()

    private fun updateStepCounter() {
        //每次回调都判断一下是否跨天

        dateChangeCleanStep()

        mStepCounterListener?.stepChange(mCount)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.type) {
            for (i in 0..2) {
                oriValues[i] = event.values[i]
            }
            gravityNew = sqrt(
                (oriValues[0] * oriValues[0] + oriValues[1] * oriValues[1] + oriValues[2] * oriValues[2]).toDouble()
            ).toFloat()
            detectorNewStep(gravityNew)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //
    }

    /*
     * 检测步子，并开始计步
     * 1.传入sersor中的数据
     * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
     * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
     * */
    private fun detectorNewStep(values: Float) {
        if (gravityOld == 0f) {
            gravityOld = values
        } else {
            if (detectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak
                timeOfNow = System.currentTimeMillis()
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                    && (peakOfWave - valleyOfWave >= ThreadValue)
                ) {
                    timeOfThisPeak = timeOfNow
                    /*
                     * 更新界面的处理，不涉及到算法
                     * 一般在通知更新界面之前，增加下面处理，为了处理无效运动：
                     * 1.连续记录10才开始计步
                     * 2.例如记录的9步用户停住超过3秒，则前面的记录失效，下次从头开始
                     * 3.连续记录了9步用户还在运动，之前的数据才有效
                     * */
                    countStep()
                }
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                    && (peakOfWave - valleyOfWave >= InitialValue)
                ) {
                    timeOfThisPeak = timeOfNow
                    ThreadValue = peakValleyThread(peakOfWave - valleyOfWave)
                }
            }
        }
        gravityOld = values
    }

    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于20
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    private fun detectorPeak(newValue: Float, oldValue: Float): Boolean {
        lastStatus = isDirectionUp
        if (newValue >= oldValue) {
            isDirectionUp = true
            continueUpCount++
        } else {
            continueUpFormerCount = continueUpCount
            continueUpCount = 0
            isDirectionUp = false
        }

        if (!isDirectionUp && lastStatus
            && (continueUpFormerCount >= 2 || oldValue >= 20)
        ) {
            peakOfWave = oldValue
            return true
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue
            return false
        } else {
            return false
        }
    }

    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    private fun peakValleyThread(value: Float): Float {
        var tempThread = ThreadValue
        if (tempCount < valueNum) {
            tempValue[tempCount] = value
            tempCount++
        } else {
            tempThread = averageValue(tempValue, valueNum)
            for (i in 1 until valueNum) {
                tempValue[i - 1] = tempValue[i]
            }
            tempValue[valueNum - 1] = value
        }
        return tempThread
    }

    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    private fun averageValue(floats: FloatArray, value: Int): Float {
        var ave = 0f
        for (i in 0 until value) {
            ave += floats[i]
        }
        ave /= valueNum
        ave = if (ave >= 8) 4.3.toFloat()
        else if (ave >= 7) 3.3.toFloat()
        else if (ave >= 4) 2.3.toFloat()
        else if (ave >= 3) 2.0.toFloat()
        else 1.3.toFloat()
        return ave
    }


    /*
     * 连续走十步才会开始计步
     * 连续走了9步以下,停留超过3秒,则计数清空
     * */
    private fun countStep() {
        this.timeOfLastPeak1 = this.timeOfThisPeak1
        this.timeOfThisPeak1 = System.currentTimeMillis()
        if (this.timeOfThisPeak1 - this.timeOfLastPeak1 <= 3000L) {
            if (this.count < 9) {
                count++
            } else if (this.count == 9) {
                count++
                this.mCount += this.count
                Mkv.put(ConstStep.CURR_STEP, mCount)
                updateStepCounter()
            } else {
                mCount++
                Mkv.put(ConstStep.CURR_STEP, mCount)
                updateStepCounter()
            }
        } else { //超时
            this.count = 1 //为1,不是0
        }
    }


    private fun setSteps(initValue: Int) {
        this.mCount = initValue
        this.count = 0
        timeOfLastPeak1 = 0
        timeOfThisPeak1 = 0
    }

    var currentStep: Int
        get() = mCount
        set(initStep) {
            setSteps(initStep)

            mCount = initStep
            Mkv.put(ConstStep.CURR_STEP, mCount)

            mTodayDate = todayDate
            Mkv.put(ConstStep.STEP_TODAY, mTodayDate)

            mStepCounterListener?.stepChange(mCount)
        }
}
