package app.android.allever.gp.quick.project.ui

import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityDeviceInfoBinding
import app.android.allever.gp.quick.project.util.BatteryUtil
import app.android.allever.gp.quick.project.util.DeviceInfo
import app.android.allever.gp.quick.project.util.NetworkSpeedMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DeviceInfoActivity: AppActivity<ActivityDeviceInfoBinding, BaseViewModel>() {

    private val networkSpeedMonitor = NetworkSpeedMonitor(1000) { download, upload ->
        mBinding.apply {
            tvDownloadSpeed.text = "$download KB/S"
            tvUploadSpeed.text = "$upload KB/S"
        }
    }
    override fun inflate() = ActivityDeviceInfoBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener { finish() }

            //基本信息
            tvDisplay.text = "${DeviceInfo.getScreenHeight()}\nx\n${DeviceInfo.getScerrnWidth()}"
            tvBrand.text = DeviceInfo.getFactory()
            tvModel.text = DeviceInfo.getModel()
            tvAndroidVersion.text = DeviceInfo.getAndroidVersion()
            tvCpu.text = DeviceInfo.getCpu()
            lifecycleScope.launch {
                while (true) {
                    delay(1000)
                    tvBootTime.text = DeviceInfo.getBootTime()
//                    log("update boot time")
                }
            }
            tvPublishDate.text = DeviceInfo.getRomPublishDate()

            //网络信息
            networkSpeedMonitor.startMonitoring()
            tvNetworkType.text = DeviceInfo.getNetWorkType()
            tvInternalIp.text = DeviceInfo.getInternalIp()

            //内存
            tvMemoryUsedRate.text = "${DeviceInfo.getMemoryUsageRate()} %"
            tvMemoryTotal.text = "${DeviceInfo.getMemorySize()} GB"
            tvMemoryAvail.text = "${DeviceInfo.getAvailMemory()} GB"

            //电池
            tvBatteryLeft.text = "${DeviceInfo.getBatteryLeft()} %"
            BatteryUtil.receiverBatteryOhterInfo(this@DeviceInfoActivity) {temp, health, mv, state ->
                tvBatteryTemp.text = "${temp} ℃"
                tvBatteryHealth.text = health
                tvBatteryMv.text = mv
                tvBatteryCharge.text = state
            }

            //存储
            tvStoreLeft.text = "${DeviceInfo.spaceLeft} %"
            tvStoreTotal.text = "${DeviceInfo.totalSpace} GB"
            tvStoreUsed.text = "${DeviceInfo.usedSpace} GB"
            tvStoreUnUse.text = "${DeviceInfo.freeSpace} GB"

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkSpeedMonitor.stopMonitoring()
    }
}