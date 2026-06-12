package app.allever.android.sample.cleaner.ui.fragment

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.FragmentMonitorBinding
import app.allever.android.sample.cleaner.monitor.BatteryMonitor
import app.allever.android.sample.cleaner.monitor.CpuMonitor
import app.allever.android.sample.cleaner.monitor.ThermalMonitor
import kotlinx.coroutines.launch

/**
 * 性能监控 Fragment
 *
 * 对应文档"性能监控"章节：
 * - CPU 使用率、频率、核心数
 * - 电池电量、状态、温度
 * - 设备温度（CPU / 电池）
 */
class MonitorFragment :
    BaseFragment<FragmentMonitorBinding, CleanerViewModel>() {

    override fun inflate(): FragmentMonitorBinding =
        FragmentMonitorBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.btnRefreshMonitor.setOnClickListener { refreshAllData() }

        // 首次自动加载
        refreshAllData()
    }

    /**
     * 刷新所有监控数据
     */
    private fun refreshAllData() {
        mBinding.btnRefreshMonitor.isEnabled = false
        mBinding.btnRefreshMonitor.text = "刷新中..."

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { loadCpuInfo() }
                launch { loadBatteryInfo() }
                launch { loadThermalInfo() }

                mBinding.btnRefreshMonitor.isEnabled = true
                mBinding.btnRefreshMonitor.text = "刷新监控数据"
            }
        }
    }

    private suspend fun loadCpuInfo() {
        val cpuInfo = CpuMonitor.getCpuInfo()

        mBinding.progressCpu.progress = cpuInfo.usagePercent.toInt()
        mBinding.tvCpuUsage.text = "使用率：${cpuInfo.formattedUsage}"
        mBinding.tvCpuInfo.text =
            "核心数：${cpuInfo.coreCount} | 频率：${cpuInfo.formattedFreq}"
    }

    private suspend fun loadBatteryInfo() {
        val batteryInfo = BatteryMonitor.getBatteryInfo()

        mBinding.progressBattery.progress = batteryInfo.level
        mBinding.tvBatteryLevel.text = "电量：${batteryInfo.formattedLevel}"

        val statusText = buildString {
            append("状态：${batteryInfo.chargeStatus}")
            append(" | 温度：${batteryInfo.formattedTemp}")
        }
        mBinding.tvBatteryInfo.text = statusText
    }

    private suspend fun loadThermalInfo() {
        val thermalInfo = ThermalMonitor.getThermalInfo()

        mBinding.tvCpuTemp.text = "CPU 温度：${thermalInfo.formattedCpuTemp}"
        mBinding.batteryTemp.text = "电池温度：${thermalInfo.formattedBatteryTemp}"

        if (thermalInfo.isOverheated) {
            setVisibility(mBinding.tvThermalWarning, true)
            mBinding.tvThermalWarning.text = "⚠ 设备温度过高，建议降温"
        } else {
            setVisibility(mBinding.tvThermalWarning, false)
        }
    }
}
