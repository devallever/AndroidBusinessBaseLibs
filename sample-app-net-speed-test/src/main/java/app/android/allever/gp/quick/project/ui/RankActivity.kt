package app.android.allever.gp.quick.project.ui

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.Config
import app.android.allever.gp.quick.project.SpeedTest
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityRankBinding
import app.android.allever.gp.quick.project.ui.adapter.RankItemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RankActivity: AppActivity<ActivityRankBinding, BaseViewModel>() {

    private val adapter by lazy {
        RankItemAdapter().apply {
            setList(SpeedTest.rankData)
        }
    }
    override fun inflate() = ActivityRankBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener {
                finish()
            }

            recyclerView.layoutManager = LinearLayoutManager(this@RankActivity)
            recyclerView.adapter = adapter

            //我的
            lifecycleScope.launch (Dispatchers.IO){
                val list = SpeedTest.getAllByOrderDownloadSpeed(false)
                if (list.isEmpty()) {
                    return@launch
                }
                val record = list[0]
                tvRank.text = Config.getMyRank()
                tvDownloadSpeed.text = "${record.downloadSpeed} Mbps"
                tvUploadSpeed.text = "${record.uploadSpeed} Mbps"
                tvTime.text =  TimeHelper.formatTimeAgo(record.time)
                tvNetworkType.text = "${record.operator} ${record.networkType}"
                tvModel.text = record.model
            }
        }
    }
}