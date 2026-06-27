package com.example.charge.ui.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.example.charge.base.BaseActivity
import com.example.charge.currency.CurrencyFlyAnimatorUtil
import com.example.charge.currency.CurrencyType
import com.example.charge.databinding.ActivityFunctionBinding
import com.example.charge.utils.ChargeStatusListener
import com.example.charge.utils.CountdownTimer
import com.example.charge.utils.AdvancedTimer
import com.example.charge.utils.log
import com.example.charge.utils.setOnSingleListener

class FunctionActivity : BaseActivity<ActivityFunctionBinding>() {

    // 充电状态监听器
    private lateinit var chargeStatusListener: ChargeStatusListener
    
    // 倒计时工具
    private lateinit var countdownTimer: CountdownTimer
    
    // 高级定时器工具
    private lateinit var advancedTimer: AdvancedTimer
    
    // 默认倒计时秒数
    private val DEFAULT_COUNTDOWN_SECONDS = 60
    
    // 追加的秒数
    private val ADD_SECONDS = 10

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityFunctionBinding {
        return ActivityFunctionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        fixStatusBar(binding.chargeStateContainer)
        // 初始化充电状态监听器
        initChargeStatusListener()
        initListener()
        
        // 初始化倒计时工具
        initCountdownTimer()
        
        // 初始化高级定时器
        initAdvancedTimer()
        
        // 初始化按钮事件
        initButtonEvents()
        initAdvancedTimerButtonEvents()
    }

    private fun initListener() {
        binding.btnAddGold.setOnSingleListener {
            CurrencyFlyAnimatorUtil.start(this, binding.currencyView, binding.root, CurrencyType.GOLD, 1500f)
        }
        binding.btnAddGreen.setOnSingleListener {
            CurrencyFlyAnimatorUtil.start(this, binding.currencyView, binding.root, CurrencyType.GREEN, 1500f)
        }

        binding.btnStartRoundProgress.setOnSingleListener {
            if (binding.roundProgressBar.tag != null) {
                (binding.roundProgressBar.tag as ValueAnimator).cancel()
            }
            val objAnim = ValueAnimator.ofFloat(100f, 0f)
            objAnim.duration = 3000
            objAnim.addUpdateListener {
                val value = objAnim.animatedValue as Float
                binding.roundProgressBar.setProgress(value.toInt())
            }
            objAnim.start()
            binding.roundProgressBar.tag = objAnim
        }
    }

    /**
     * 初始化充电状态监听器
     */
    private fun initChargeStatusListener() {
        chargeStatusListener = ChargeStatusListener(this)

        // 开始监听充电状态变化
        chargeStatusListener.startListening(object :
            ChargeStatusListener.OnChargeStatusChangeListener {
            override fun onChargeStatusChanged(
                isCharging: Boolean,
                chargeType: String,
                batteryLevel: Int,
                batteryTemperature: Float,
                batteryVoltage: Int
            ) {
                // 更新UI显示充电状态信息
                updateChargeStatusUI(
                    isCharging,
                    chargeType,
                    batteryLevel,
                    batteryTemperature,
                    batteryVoltage
                )

                // 打印日志
                log(
                    "ChargeDemo",
                    "充电状态变化 - 充电中:$isCharging, 类型:$chargeType, 电量:$batteryLevel%, " +
                            "温度:${batteryTemperature}°C, 电压:${batteryVoltage}mV"
                )
            }
        })

        // 立即获取并显示当前电池状态
        val currentStatus = chargeStatusListener.getCurrentBatteryStatus()
        updateChargeStatusUI(
            currentStatus.isCharging,
            currentStatus.chargeType,
            currentStatus.batteryLevel,
            currentStatus.batteryTemperature,
            currentStatus.batteryVoltage
        )
    }

    /**
     * 更新充电状态UI显示
     */
    private fun updateChargeStatusUI(
        isCharging: Boolean,
        chargeType: String,
        batteryLevel: Int,
        batteryTemperature: Float,
        batteryVoltage: Int
    ) {
        binding.tvChargingStatus.text = if (isCharging) "充电中" else "未充电"
        binding.tvChargeType.text = chargeType
        binding.tvBatteryLevel.text = "${batteryLevel}%"
        binding.tvBatteryTemperature.text = "${batteryTemperature}°C"
        binding.tvBatteryVoltage.text = "${batteryVoltage}mV"
    }
    
    /**
     * 初始化倒计时工具
     */
    private fun initCountdownTimer() {
        countdownTimer = CountdownTimer()
        
        // 初始化倒计时显示
        updateCountdownDisplay(DEFAULT_COUNTDOWN_SECONDS)
        updateCountdownStatus("状态: 未开始")
    }
    
    /**
     * 初始化高级定时器
     */
    private fun initAdvancedTimer() {
        advancedTimer = AdvancedTimer()
        
        // 设置监听器
        advancedTimer.setListener(object : AdvancedTimer.OnTimerTickListener {
            override fun onSecondTick(seconds: Long) {
                // 每秒回调 - 更新时间显示
                val elapsedTime = advancedTimer.getElapsedTime()
                binding.tvAdvancedTime.text = formatTimeWithMillis(elapsedTime)
                log("ChargeDemo", "每秒回调 - 毫秒:$elapsedTime")
            }

            override fun onIntervalTick(milliseconds: Long, progress: Int) {
                // 间隔回调 - 更新进度显示
                binding.tvAdvancedProgress.text = "$progress%"
                log("ChargeDemo", "间隔回调 - 毫秒:$milliseconds, 进度:$progress%")
            }
        })
        
        // 初始化显示
        binding.tvAdvancedState.text = "停止"
        binding.tvAdvancedState.setTextColor(android.graphics.Color.RED)
        binding.tvAdvancedTime.text = "00:00:00.000"
        binding.tvAdvancedProgress.text = "0%"
    }
    
    /**
     * 初始化高级定时器按钮事件
     */
    private fun initAdvancedTimerButtonEvents() {
        binding.btnStartAdvancedTimer?.setOnSingleListener {
            // 获取用户设置的间隔时间
            val intervalText = binding.etInterval?.text.toString()
            val interval = try {
                intervalText.toLong()
            } catch (e: NumberFormatException) {
                1000L // 默认1秒
            }

            // 设置间隔并启动定时器
            advancedTimer.setInterval(interval).start()
            binding.tvAdvancedState.text = "运行中"
            binding.tvAdvancedState.setTextColor(android.graphics.Color.GREEN)
        }

        binding.btnStopAdvancedTimer?.setOnSingleListener {
            advancedTimer.stop()
            binding.tvAdvancedState.text = "已停止"
            binding.tvAdvancedState.setTextColor(android.graphics.Color.RED)
        }

        binding.btnResetAdvancedTimer?.setOnSingleListener {
            advancedTimer.reset()
            binding.tvAdvancedState.text = "停止"
            binding.tvAdvancedState.setTextColor(android.graphics.Color.RED)
            binding.tvAdvancedTime.text = "00:00:00.000"
            binding.tvAdvancedProgress.text = "0%"
        }
    }
    
    /**
     * 初始化按钮事件
     */
    private fun initButtonEvents() {
        // 开始按钮
        binding.btnStart.setOnSingleListener {
            startCountdown()
        }
        
        // 暂停按钮
        binding.btnPause.setOnSingleListener {
            pauseCountdown()
        }
        
        // 恢复按钮
        binding.btnResume.setOnSingleListener {
            resumeCountdown()
        }
        
        // 追加时间按钮
        binding.btnAddTime.setOnSingleListener {
            addCountdownTime()
        }
    }
    
    /**
     * 开始倒计时
     */
    private fun startCountdown() {
        countdownTimer.start(DEFAULT_COUNTDOWN_SECONDS, object : CountdownTimer.OnCountdownListener {
            override fun onTick(remainingSeconds: Int, progress: Int) {
                // 更新倒计时显示
                updateCountdownDisplay(remainingSeconds)
                updateCountdownStatus("状态: 倒计时中 - 进度: ${100-progress}%")
            }
            
            override fun onFinish() {
                // 倒计时结束
                updateCountdownDisplay(0)
                updateCountdownStatus("状态: 倒计时结束")
                Toast.makeText(this@FunctionActivity, "倒计时结束！", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    /**
     * 暂停倒计时
     */
    private fun pauseCountdown() {
        if (countdownTimer.isCountingDown()) {
            countdownTimer.pause()
            updateCountdownStatus("状态: 已暂停")
        }
    }
    
    /**
     * 恢复倒计时
     */
    private fun resumeCountdown() {
        if (!countdownTimer.isCountingDown() && countdownTimer.getRemainingSeconds() > 0) {
            countdownTimer.resume()
            updateCountdownStatus("状态: 倒计时中")
        }
    }
    
    /**
     * 追加倒计时时间
     */
    private fun addCountdownTime() {
        countdownTimer.addSeconds(ADD_SECONDS)
        val remainingSeconds = countdownTimer.getRemainingSeconds()
        updateCountdownDisplay(remainingSeconds)
        log("已追加${ADD_SECONDS}秒，剩余${remainingSeconds}秒")
    }
    
    /**
     * 格式化时间，包含毫秒
     */
    @SuppressLint("DefaultLocale")
    private fun formatTimeWithMillis(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val remainingMillis = milliseconds % 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, remainingMillis)
    }
    
    /**
     * 更新倒计时显示
     */
    @SuppressLint("DefaultLocale")
    private fun updateCountdownDisplay(seconds: Int) {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val formattedTime = String.format("%02d:%02d", minutes, remainingSeconds)
        binding.tvCountdown.text = formattedTime
    }
    
    /**
     * 更新倒计时状态
     */
    private fun updateCountdownStatus(status: String) {
        binding.tvCountdownStatus.text = status
    }

    override fun onDestroy() {
        super.onDestroy()
        // 在Activity销毁时停止监听，避免内存泄漏
        chargeStatusListener.stopListening()
        
        // 释放倒计时工具资源
        countdownTimer.release()
        
        // 释放高级定时器资源
        advancedTimer.release()
    }
}