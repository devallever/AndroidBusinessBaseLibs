package app.android.allever.gp.quick.project.ui

import android.text.format.Formatter
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.SpeedTest
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityDetailBinding

class DetailActivity : AppActivity<ActivityDetailBinding, BaseViewModel>() {
    override fun inflate() = ActivityDetailBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener {
                finish()
            }

            SpeedTest.record?.let {
                val band = it.downloadSpeed.toInt()
                tvBand.text = band.toString()
                tvDownloadBytes.text =
                    Formatter.formatFileSize(this@DetailActivity, it.downloadBytes)
                tvUploadBytes.text = Formatter.formatFileSize(this@DetailActivity, it.uploadBytes)
                tvDownloadResult.text = it.downloadSpeed.toString()
                tvUploadResult.text = it.uploadSpeed.toString()
                tvNetworkType.text = "网络：${it.networkType}"
                tvOperator.text = "运营商：${it.operator}"
                tvServer.text = "服务器：${it.serverName}"
                tvExtraIp.text = "外部IP：${it.ip}"
                tvInternalIp.text = "内部IP：${it.internalIp}"
                tvModel.text = "机型：${it.model}"
                tvTime.text = "时间：${TimeHelper.formatDateTime(it.time)}"
                tvLocation.text = "位置：${it.location}"

                if (band >= 100) {
                    ivSpeed.setImageResource(R.drawable.icon_plane)
                } else if (band in 50..99) {
                    ivSpeed.setImageResource(R.drawable.icon_car)
                } else if (band in 20..49) {
                    ivSpeed.setImageResource(R.drawable.icon_motorcycle)
                } else if (band in 10..19) {
                    ivSpeed.setImageResource(R.drawable.icon_bicyce)
                } else {
                    ivSpeed.setImageResource(R.drawable.icon_snail)
                }
            }
        }
    }
}